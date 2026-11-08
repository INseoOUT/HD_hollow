package com.hdhollow.studio.phone

import android.content.Context
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.os.ParcelFileDescriptor
import android.util.Log
import com.hdhollow.studio.shared.Aoa
import com.hdhollow.studio.shared.StreamTransport
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 폰이 USB 액세서리 모드로 차와 붙는 경로(P13, 2차). 차가 AOA 로 모드를 바꾸면 시스템이
 * USB_ACCESSORY_ATTACHED 로 MainActivity 를 띄우고, 송출 중이면 서비스가 여기로 액세서리를 연다.
 * 실차 미확인. 액세서리가 없거나 권한이 없으면 아무것도 하지 않는다.
 */
object UsbAccessoryLink {
    private const val TAG = "UsbAccessoryLink"

    /** 우리 차 앱이 만든 액세서리가 붙어 있고 권한이 있으면 세션으로 붙인다. 붙였으면 true. */
    fun attachIfPresent(context: Context, link: StudioLink): Boolean {
        val usb = context.getSystemService(UsbManager::class.java) ?: return false
        val accessory = usb.accessoryList?.firstOrNull(::isOurs) ?: return false
        if (!usb.hasPermission(accessory)) {
            Log.d(TAG, "액세서리 권한 없음: ${accessory.model}")
            return false
        }
        val pfd: ParcelFileDescriptor = usb.openAccessory(accessory) ?: return false
        val fd = pfd.fileDescriptor
        val transport = StreamTransport(FileInputStream(fd), FileOutputStream(fd), "aoa") { runCatching { pfd.close() } }
        return runCatching { link.attach(transport); true }
            .onFailure { Log.w(TAG, "액세서리 세션 실패", it); runCatching { pfd.close() } }
            .getOrDefault(false)
    }

    fun isOurs(a: UsbAccessory): Boolean = a.manufacturer == Aoa.MANUFACTURER && a.model == Aoa.MODEL
}
