package com.hdhollow.studio.car

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.hdhollow.studio.shared.Aoa
import com.hdhollow.studio.shared.Transport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlin.math.min

/**
 * 차가 USB 호스트로 폰을 여는 경로(C15, 2차). 폰을 AOA 액세서리 모드로 바꾸고 bulk 엔드포인트를 [Transport] 로 감싼다.
 *
 * 순서: 이미 액세서리 모드인 장치(Google VID, PID 0x2D00~)가 있으면 그것을 연다. 없으면 붙어 있는 장치에 권한을 받아
 * AOA 문자열을 보내고 START 를 요청한 뒤, 다시 열거될 때까지 잠깐 기다린다.
 * 실차에서 서드파티 앱이 USB 호스트 API 를 쓸 수 있는지, 시스템(Android Auto)이 폰을 먼저 가져가지 않는지는 미확인.
 * 장치가 없거나 권한이 없으면 null 을 돌려주고 호출자는 TCP 로 넘어간다.
 */
class UsbHostLink(private val context: Context) {

    private val usb: UsbManager? = context.getSystemService(UsbManager::class.java)

    suspend fun open(): Transport? {
        val manager = usb ?: return null
        if (!context.packageManager.hasSystemFeature("android.hardware.usb.host")) return null
        val devices = runCatching { manager.deviceList.values.toList() }.getOrDefault(emptyList())
        if (devices.isEmpty()) return null

        devices.firstOrNull(::isAccessoryMode)?.let { return openAccessory(manager, it) }

        // 액세서리 모드가 아닌 장치 하나를 골라 모드를 바꿔 본다(폰이 아닐 수도 있어 실패는 조용히)
        val candidate = devices.first()
        if (!ensurePermission(manager, candidate)) return null
        if (!switchToAccessory(manager, candidate)) return null
        delay(1500) // 재열거 대기
        val switched = runCatching { manager.deviceList.values.firstOrNull(::isAccessoryMode) }.getOrNull() ?: return null
        return openAccessory(manager, switched)
    }

    private fun isAccessoryMode(d: UsbDevice): Boolean = d.vendorId == Aoa.GOOGLE_VID && d.productId in Aoa.ACCESSORY_PIDS

    private suspend fun ensurePermission(manager: UsbManager, device: UsbDevice): Boolean {
        if (manager.hasPermission(device)) return true
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { cont ->
                val action = "${context.packageName}.USB_PERMISSION"
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(c: Context, intent: Intent) {
                        runCatching { context.unregisterReceiver(this) }
                        if (cont.isActive) cont.resume(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    }
                }
                val filter = IntentFilter(action)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    @Suppress("UnspecifiedRegisterReceiverFlag")
                    context.registerReceiver(receiver, filter)
                }
                // 시스템이 결과 extra 를 채우므로 MUTABLE 이어야 한다
                val pi = PendingIntent.getBroadcast(
                    context, 0, Intent(action).setPackage(context.packageName),
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                manager.requestPermission(device, pi)
                cont.invokeOnCancellation { runCatching { context.unregisterReceiver(receiver) } }
            }
        } ?: false
    }

    private suspend fun switchToAccessory(manager: UsbManager, device: UsbDevice): Boolean = withContext(Dispatchers.IO) {
        val conn = manager.openDevice(device) ?: return@withContext false
        try {
            val buf = ByteArray(2)
            val n = conn.controlTransfer(0xC0, Aoa.REQ_GET_PROTOCOL, 0, 0, buf, 2, 1000)
            if (n < 2 || (buf[0].toInt() or (buf[1].toInt() shl 8)) < 1) return@withContext false
            fun send(index: Int, s: String) {
                val b = (s + "\u0000").toByteArray(Charsets.UTF_8)
                conn.controlTransfer(0x40, Aoa.REQ_SEND_STRING, 0, index, b, b.size, 1000)
            }
            send(Aoa.STR_MANUFACTURER, Aoa.MANUFACTURER)
            send(Aoa.STR_MODEL, Aoa.MODEL)
            send(Aoa.STR_DESCRIPTION, Aoa.DESCRIPTION)
            send(Aoa.STR_VERSION, Aoa.VERSION)
            send(Aoa.STR_URI, Aoa.URI)
            send(Aoa.STR_SERIAL, Aoa.SERIAL)
            conn.controlTransfer(0x40, Aoa.REQ_START, 0, 0, null, 0, 1000) >= 0
        } catch (e: Exception) {
            Log.w(TAG, "AOA 전환 실패", e)
            false
        } finally {
            conn.close()
        }
    }

    private suspend fun openAccessory(manager: UsbManager, device: UsbDevice): Transport? {
        if (!ensurePermission(manager, device)) return null
        val conn = manager.openDevice(device) ?: return null
        val intf: UsbInterface = (0 until device.interfaceCount).map(device::getInterface)
            .firstOrNull { it.interfaceClass == UsbConstants.USB_CLASS_VENDOR_SPEC } ?: device.getInterface(0)
        if (!conn.claimInterface(intf, true)) {
            conn.close()
            return null
        }
        var epIn: UsbEndpoint? = null
        var epOut: UsbEndpoint? = null
        for (i in 0 until intf.endpointCount) {
            val ep = intf.getEndpoint(i)
            if (ep.type != UsbConstants.USB_ENDPOINT_XFER_BULK) continue
            if (ep.direction == UsbConstants.USB_DIR_IN) epIn = ep else epOut = ep
        }
        if (epIn == null || epOut == null) {
            conn.releaseInterface(intf)
            conn.close()
            return null
        }
        Log.d(TAG, "AOA 연결 ${device.deviceName}")
        return AoaTransport(conn, intf, epIn, epOut)
    }

    companion object {
        private const val TAG = "UsbHostLink"
    }
}

/** bulk IN/OUT 을 스트림으로. 읽기는 타임아웃을 돌며 기다린다(세션의 무수신 타임아웃이 끊김을 판단). */
private class AoaTransport(
    private val conn: UsbDeviceConnection,
    private val intf: UsbInterface,
    private val epIn: UsbEndpoint,
    private val epOut: UsbEndpoint,
) : Transport {
    @Volatile private var closed = false
    private val inBuf = ByteArray(16 * 1024)
    private var inPos = 0
    private var inLen = 0

    override val name: String = "aoa"

    override val input: InputStream = object : InputStream() {
        override fun read(): Int {
            val b = ByteArray(1)
            return if (read(b, 0, 1) == 1) b[0].toInt() and 0xFF else -1
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            while (inPos >= inLen) {
                if (closed) return -1
                val n = conn.bulkTransfer(epIn, inBuf, inBuf.size, 500)
                if (n > 0) {
                    inPos = 0
                    inLen = n
                } else if (closed) {
                    return -1
                } // n <= 0: 타임아웃으로 보고 다시 기다린다
            }
            val n = min(len, inLen - inPos)
            System.arraycopy(inBuf, inPos, b, off, n)
            inPos += n
            return n
        }
    }

    override val output: OutputStream = object : OutputStream() {
        override fun write(b: Int) = write(byteArrayOf(b.toByte()), 0, 1)

        override fun write(b: ByteArray, off: Int, len: Int) {
            var o = off
            var remaining = len
            while (remaining > 0) {
                if (closed) throw IOException("aoa closed")
                val chunk = min(remaining, 16 * 1024)
                val n = conn.bulkTransfer(epOut, b.copyOfRange(o, o + chunk), chunk, 2000)
                if (n < 0) throw IOException("bulk write 실패")
                o += n
                remaining -= n
            }
        }
    }

    override fun close() {
        closed = true
        runCatching { conn.releaseInterface(intf) }
        runCatching { conn.close() }
    }
}
