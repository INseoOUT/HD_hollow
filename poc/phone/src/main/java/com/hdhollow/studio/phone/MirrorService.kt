package com.hdhollow.studio.phone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import androidx.core.app.NotificationCompat
import com.hdhollow.studio.shared.Hello
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.Protocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * 폰 화면을 캡처해 차로 보내는 포그라운드 서비스. plan/work-plan.md 3절 P2~P7.
 *
 * API 34+ 순서: startForeground(mediaProjection 타입) → getMediaProjection → registerCallback → ImageReader → createVirtualDisplay.
 * 캡처는 긴 변 [Protocol.FRAME_MAX_SIDE]px 이하로 축소하고 [Protocol.FRAME_MAX_FPS]fps 로 제한한다.
 * 상태([status])와 조명 값([lighting])은 companion 의 StateFlow 로 액티비티·알림과 공유한다.
 */
class MirrorService : Service() {

    data class Status(
        val running: Boolean = false,
        val clients: Int = 0,
        val fps: Int = 0,
        val frame: String = "", // 예: "540×1200 · 48 KB"
        val error: String? = null,
    )

    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var reusable: Bitmap? = null
    private val captureThread = HandlerThread("mirror-capture").apply { start() }
    private val handler = Handler(captureThread.looper)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var link: StudioLink

    private var capW = 0
    private var capH = 0
    private var lastSentNs = 0L
    private var fpsCount = 0
    private var fpsWindowNs = 0L

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            if (displayId != Display.DEFAULT_DISPLAY) return
            val (w, h) = captureSize()
            if (w != capW || h != capH) handler.post { recreateCapture() } // 회전(P7)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        link = StudioLink(
            hello = { val m = realMetrics(); Hello(Protocol.ROLE_PHONE, m.widthPixels, m.heightPixels) },
            onLight = { p -> _lighting.value = p }, // 차에서 온 값. src=car 라 다시 보내지 않는다
        )
        scope.launch { link.clients.collect { n -> _status.update { it.copy(clients = n) } } }
        // 폰에서 바뀐 조명 값을 차로. 차에서 온 값(src=car)은 되돌려 보내지 않는다. 플로팅 컨트롤 표시도 갱신
        scope.launch {
            lighting.collect { p ->
                if (p.src == Protocol.ROLE_PHONE) link.sendLight(p)
                floatingControl.render(p)
            }
        }
        // 플로팅 컨트롤(P12): 송출 중이고 켜 달라고 했을 때만. 권한이 없으면 show() 가 아무것도 안 한다
        scope.launch {
            combine(floating, status) { f, s -> f.on && s.running }.collect { on ->
                if (on) floatingControl.show() else floatingControl.hide()
            }
        }
    }

    private val floatingControl by lazy { FloatingControl(this) }
    private var nsdListener: NsdManager.RegistrationListener? = null

    /** 무선 모드(P14): 같은 Wi-Fi·핫스팟의 차가 찾을 수 있게 `_carstudio._tcp` 를 광고한다. */
    private fun advertise() {
        val nsd = getSystemService(NsdManager::class.java) ?: return
        val info = NsdServiceInfo().apply {
            serviceName = Protocol.NSD_SERVICE_NAME
            serviceType = Protocol.NSD_SERVICE_TYPE
            port = Protocol.PORT
        }
        val l = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(i: NsdServiceInfo) = Log.d(TAG, "NSD 등록 ${i.serviceName}").let {}
            override fun onRegistrationFailed(i: NsdServiceInfo, errorCode: Int) = Log.w(TAG, "NSD 등록 실패 $errorCode").let {}
            override fun onServiceUnregistered(i: NsdServiceInfo) {}
            override fun onUnregistrationFailed(i: NsdServiceInfo, errorCode: Int) {}
        }
        runCatching { nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, l); nsdListener = l }
            .onFailure { Log.w(TAG, "NSD 등록 예외", it) }
    }

    private fun unadvertise() {
        nsdListener?.let { l -> runCatching { getSystemService(NsdManager::class.java).unregisterService(l) } }
        nsdListener = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            // 알림 버튼(P9). 촬영 앱이 앞에 있어도 상태바에서 색온도를 바꾼다. 알림 액션은 3개까지라 밝기는 플로팅 컨트롤·앱 화면에서
            ACTION_WARMER -> updateLighting { it.copy(kelvin = it.kelvin - LightingParams.KELVIN_NOTIFICATION_STEP, preset = null) }
            ACTION_COOLER -> updateLighting { it.copy(kelvin = it.kelvin + LightingParams.KELVIN_NOTIFICATION_STEP, preset = null) }
            // 송출 중에 차가 USB 액세서리로 붙었을 때(P13). 액티비티가 USB_ACCESSORY_ATTACHED 를 받고 보낸다
            ACTION_ATTACH_USB -> if (projection != null) UsbAccessoryLink.attachIfPresent(this, link)
            ACTION_START -> {
                startForegroundCompat()
                val code = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                @Suppress("DEPRECATION")
                val data = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                if (data == null) {
                    fail("화면 공유 동의 결과가 없습니다")
                } else {
                    beginProjection(code, data)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun beginProjection(resultCode: Int, data: Intent) {
        if (projection != null) return
        val mpm = getSystemService(MediaProjectionManager::class.java)
        val p = try {
            mpm.getMediaProjection(resultCode, data)
        } catch (e: SecurityException) {
            return fail("화면 캡처 권한 오류: ${e.message}")
        } ?: return fail("화면 캡처를 시작하지 못했습니다")
        projection = p
        // API 34+: createVirtualDisplay 전에 콜백을 등록해야 한다. 사용자가 상태바에서 중지하면 onStop
        p.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.d(TAG, "MediaProjection 중지됨")
                projection = null
                stopSelf()
            }
        }, handler)
        runCatching { link.start() }.onFailure { return fail("포트 ${Protocol.PORT} 를 열지 못했습니다: ${it.message}") }
        advertise()
        UsbAccessoryLink.attachIfPresent(this, link) // 이미 USB 액세서리로 붙어 있으면 그 경로도 연다
        getSystemService(DisplayManager::class.java).registerDisplayListener(displayListener, handler)
        handler.post { recreateCapture() }
        _status.update { it.copy(running = true, error = null) }
    }

    /** 가상 디스플레이·ImageReader 를 (다시) 만든다. 회전으로 크기가 바뀔 때도 쓴다. */
    private fun recreateCapture() {
        val p = projection ?: return
        val (w, h) = captureSize()
        val dpi = realMetrics().densityDpi
        capW = w
        capH = h
        val newReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        newReader.setOnImageAvailableListener({ onImage(it) }, handler)
        val old = reader
        reader = newReader
        val d = display
        if (d == null) {
            display = p.createVirtualDisplay(
                "CarStudio", w, h, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, newReader.surface, null, handler
            )
        } else {
            d.resize(w, h, dpi)
            d.surface = newReader.surface
        }
        old?.close()
        reusable = null
        Log.d(TAG, "캡처 ${w}x${h} @${dpi}dpi")
    }

    private fun onImage(r: ImageReader) {
        val img = r.acquireLatestImage() ?: return
        try {
            val now = SystemClock.elapsedRealtimeNanos()
            if (now - lastSentNs < FRAME_INTERVAL_NS) return
            val plane = img.planes[0]
            val rowStride = plane.rowStride
            val pixelStride = plane.pixelStride
            val paddedW = rowStride / pixelStride
            var bmp = reusable
            if (bmp == null || bmp.width != paddedW || bmp.height != img.height) {
                bmp = Bitmap.createBitmap(paddedW, img.height, Bitmap.Config.ARGB_8888)
                reusable = bmp
            }
            bmp.copyPixelsFromBuffer(plane.buffer)
            val cropped = if (paddedW != img.width) Bitmap.createBitmap(bmp, 0, 0, img.width, img.height) else bmp
            val out = ByteArrayOutputStream(96 * 1024)
            cropped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            if (cropped !== bmp) cropped.recycle()
            val jpeg = out.toByteArray()
            link.publishFrame(jpeg)
            lastSentNs = now
            fpsCount++
            if (now - fpsWindowNs >= 1_000_000_000L) {
                val fps = fpsCount
                fpsCount = 0
                fpsWindowNs = now
                _status.update { it.copy(fps = fps, frame = "${img.width}×${img.height} · ${jpeg.size / 1024} KB") }
            }
        } catch (e: Exception) {
            Log.w(TAG, "프레임 처리 실패", e)
        } finally {
            img.close()
        }
    }

    /** 폰 화면을 긴 변 1200px 이하로 축소한 캡처 크기(짝수). */
    private fun captureSize(): Pair<Int, Int> {
        val m = realMetrics()
        val scale = min(1f, Protocol.FRAME_MAX_SIDE.toFloat() / max(m.widthPixels, m.heightPixels))
        val w = ((m.widthPixels * scale).toInt() / 2) * 2
        val h = ((m.heightPixels * scale).toInt() / 2) * 2
        return max(w, 2) to max(h, 2)
    }

    @Suppress("DEPRECATION")
    private fun realMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY).getRealMetrics(dm)
        return dm
    }

    private fun fail(message: String) {
        Log.w(TAG, message)
        _status.update { it.copy(error = message) }
        stopSelf()
    }

    override fun onDestroy() {
        runCatching { getSystemService(DisplayManager::class.java).unregisterDisplayListener(displayListener) }
        display?.release()
        display = null
        reader?.close()
        reader = null
        projection?.stop()
        projection = null
        unadvertise()
        floatingControl.hide()
        link.stop()
        scope.cancel()
        captureThread.quitSafely()
        _status.update { it.copy(running = false, clients = 0, fps = 0, frame = "") }
        super.onDestroy()
    }

    // ---- 알림 ----

    private fun startForegroundCompat() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "화면 송출", NotificationManager.IMPORTANCE_LOW).apply {
                description = "차량으로 화면을 보내는 동안 표시됩니다"
            }
        )
        val n = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(NOTIFICATION_ID, n)
        }
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("차량으로 화면 송출 중")
            .setContentText("Car Studio 가 폰 화면을 차 화면에 보여 줍니다")
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, "따뜻하게", servicePending(ACTION_WARMER, 2))
            .addAction(0, "차갑게", servicePending(ACTION_COOLER, 3))
            .addAction(0, "중지", servicePending(ACTION_STOP, 1))
            .build()
    }

    private fun servicePending(action: String, requestCode: Int): PendingIntent = PendingIntent.getService(
        this, requestCode, Intent(this, MirrorService::class.java).setAction(action), PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        private const val TAG = "MirrorService"
        private const val CHANNEL = "mirror"
        private const val NOTIFICATION_ID = 1
        private const val JPEG_QUALITY = 65
        private const val FRAME_INTERVAL_NS = 1_000_000_000L / Protocol.FRAME_MAX_FPS

        const val ACTION_START = "com.hdhollow.studio.phone.START"
        const val ACTION_STOP = "com.hdhollow.studio.phone.STOP"
        const val ACTION_WARMER = "com.hdhollow.studio.phone.WARMER"
        const val ACTION_COOLER = "com.hdhollow.studio.phone.COOLER"
        const val ACTION_ATTACH_USB = "com.hdhollow.studio.phone.ATTACH_USB"

        /** 플로팅 컨트롤 요청. nonce 는 권한을 받고 돌아왔을 때 같은 값이라도 다시 시도하게 한다. */
        data class FloatingRequest(val on: Boolean = false, val nonce: Int = 0)

        private val _floating = MutableStateFlow(FloatingRequest())
        val floating: StateFlow<FloatingRequest> = _floating.asStateFlow()

        fun setFloating(on: Boolean) = _floating.update { FloatingRequest(on, it.nonce + 1) }
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_RESULT_DATA = "result_data"

        private val _status = MutableStateFlow(Status())
        val status: StateFlow<Status> = _status.asStateFlow()

        private val _lighting = MutableStateFlow(LightingParams(src = Protocol.ROLE_PHONE))
        /** 폰과 차가 공유하는 조명 값. src 가 phone 이면 폰에서 바꾼 것(차로 보냄), car 면 차에서 온 것. */
        val lighting: StateFlow<LightingParams> = _lighting.asStateFlow()

        fun startIntent(context: Context, resultCode: Int, data: Intent): Intent =
            Intent(context, MirrorService::class.java).setAction(ACTION_START)
                .putExtra(EXTRA_RESULT_CODE, resultCode).putExtra(EXTRA_RESULT_DATA, data)

        fun stopIntent(context: Context): Intent = Intent(context, MirrorService::class.java).setAction(ACTION_STOP)

        /** 폰에서 조명 값을 바꾼다. 서비스가 떠 있으면 차로 보낸다. */
        fun updateLighting(change: (LightingParams) -> LightingParams) {
            _lighting.update { change(it).normalized().copy(src = Protocol.ROLE_PHONE, ts = System.currentTimeMillis()) }
        }
    }
}
