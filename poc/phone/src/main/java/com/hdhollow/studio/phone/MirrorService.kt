package com.hdhollow.studio.phone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.hdhollow.studio.shared.Scene
import com.hdhollow.studio.shared.Scenes
import com.hdhollow.studio.shared.ShareStatus
import com.hdhollow.studio.shared.StudioCommand
import com.hdhollow.studio.shared.StudioState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

/**
 * 폰 화면을 캡처해 차로 보내는 포그라운드 서비스. plan/work-plan.md 3절 P2~P7.
 *
 * API 34+ 순서: startForeground(mediaProjection 타입) → getMediaProjection → registerCallback → ImageReader → createVirtualDisplay.
 * 가상 디스플레이는 정사각형(한 변 [Protocol.FRAME_MAX_SIDE]px 이하) 하나를 한 번만 만들고, 그 안에 AUTO_MIRROR 가 가운데 맞춘
 * 폰 화면 영역([MirrorFit])만 잘라 [Protocol.FRAME_MAX_FPS]fps 로 보낸다. 회전해도 가상 디스플레이를 다시 만들거나 resize 하지 않는다.
 * 상태([status])와 조명 값([lighting]), 차와 같이 보는 앱 상태([studio])는 companion 의 StateFlow 로 액티비티·알림과 공유한다.
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

    private var side = 0 // 정사각형 가상 디스플레이 한 변
    @Volatile private var content = MirrorFit.Rect(0, 0, 0, 0) // 그 안에서 폰 화면이 차지하는 영역
    private val jpegOut = ByteArrayOutputStream(128 * 1024) // 프레임마다 새로 만들지 않는다
    private var lastSentNs = 0L
    private var fpsCount = 0
    private var fpsWindowNs = 0L
    private var copyNs = 0L // 1초 창의 단계별 처리 시간 합(fps 병목 측정용 로그)
    private var compressNs = 0L
    private var sendNs = 0L
    @Volatile private var contentVisible = true // "앱 하나만 공유"에서 그 앱이 앞에 있는가(Android 14+ 콜백)
    private var userStopped = false // 사용자가 '중지'를 눌렀다(공유 앱 종료 등과 구분)

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            // 회전(P7): 가상 디스플레이는 그대로 두고 내용 영역만 다시 계산한다. 회전 중 metrics 가 늦게 바뀌는 경우에 대비해 300ms 뒤 한 번 더
            if (displayId != Display.DEFAULT_DISPLAY) return
            handler.post { refreshContent() }
            handler.postDelayed({ refreshContent() }, 300)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        init(this)
        link = StudioLink(
            hello = { val m = realMetrics(); Hello(Protocol.ROLE_PHONE, m.widthPixels, m.heightPixels, now = System.currentTimeMillis()) },
            onLight = { p -> acceptFromCar(p) }, // 차에서 온 값. 더 최신일 때만 받고, src=car 라 다시 보내지 않는다
            current = { _lighting.value }, // 차가 붙으면 내 값을 보내 어느 쪽이 최신인지 가리게 한다
            status = { ShareStatus(visible = contentVisible) },
            onState = { s -> _studio.value = s.copy(src = Protocol.ROLE_CAR) }, // 차가 기준이라 그대로 받는다
        )
        scope.launch { link.clients.collect { n -> _status.update { it.copy(clients = n) } } }
        // 폰에서 바뀐 조명 값을 차로. 차에서 온 값(src=car)은 되돌려 보내지 않는다. 플로팅 컨트롤 표시도 갱신
        scope.launch {
            lighting.collect { p ->
                if (p.src == Protocol.ROLE_PHONE) link.sendLight(p)
                floatingControl.render(p)
            }
        }
        // 폰에서 바꾼 앱 상태(src=phone)와 프리셋 부탁을 차로. 차에서 온 상태는 되돌려 보내지 않는다
        scope.launch { studio.collect { s -> if (s.src == Protocol.ROLE_PHONE) link.sendState(s) } }
        scope.launch { commands.collect { c -> link.sendCommand(c) } }
        // 플로팅 컨트롤(P12): 송출 중이고 켜 달라고 했을 때만. 권한이 없으면 show() 가 아무것도 안 한다
        scope.launch {
            combine(floating, status) { f, s -> f.on && s.running }.collect { on ->
                if (on) floatingControl.show() else floatingControl.hide()
            }
        }
    }

    private val floatingControl by lazy { FloatingControl(this) }
    private var nsdListener: NsdManager.RegistrationListener? = null

    /** 무선 모드(P14): 같은 Wi-Fi·핫스팟의 차가 찾을 수 있게 `_selene._tcp` 를 광고한다. */
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
                userStopped = true
                link.sendStatus(ShareStatus(stopped = true, reason = "user")) // 세션이 닫히기 전에 나간다(LinkSession.close 가 잠깐 기다림)
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
        // API 34+: createVirtualDisplay 전에 콜백을 등록해야 한다. 사용자가 상태바에서 중지하거나 공유한 앱이 닫히면 onStop
        p.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.d(TAG, "MediaProjection 중지됨 (사용자 중지=$userStopped)")
                projection = null
                if (!userStopped) {
                    // 공유 앱 종료·시스템 중지: 차에 '공유 끝남'을 알리고, 폰에는 탭 한 번으로 다시 시작하는 알림을 띄운다
                    link.sendStatus(ShareStatus(stopped = true, reason = "projection"))
                    notifyEnded()
                }
                stopSelf()
            }

            // Android 14+ "앱 하나만 공유": 그 앱이 가려지면 false 가 오고 프레임은 검게 나온다. 차가 안내 문구를 띄우게 알린다
            override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
                Log.d(TAG, "공유 내용 보임=$isVisible")
                contentVisible = isVisible
                link.sendStatus(ShareStatus(visible = isVisible))
            }

            override fun onCapturedContentResize(width: Int, height: Int) {
                Log.d(TAG, "공유 내용 크기 ${width}x$height")
            }
        }, handler)
        runCatching { link.start() }.onFailure { return fail("포트 ${Protocol.PORT} 를 열지 못했습니다: ${it.message}") }
        advertise()
        UsbAccessoryLink.attachIfPresent(this, link) // 이미 USB 액세서리로 붙어 있으면 그 경로도 연다
        getSystemService(DisplayManager::class.java).registerDisplayListener(displayListener, handler)
        handler.post { createCapture() }
        _status.update { it.copy(running = true, error = null) }
    }

    /** 정사각형 가상 디스플레이·ImageReader 를 만든다(한 번만). 회전해도 그대로 두고 내용 영역만 바뀐다. */
    private fun createCapture() {
        val p = projection ?: return
        if (display != null) return
        val m = realMetrics()
        side = MirrorFit.side(m.widthPixels, m.heightPixels, maxSide)
        val r = ImageReader.newInstance(side, side, PixelFormat.RGBA_8888, 2)
        r.setOnImageAvailableListener({ onImage(it) }, handler)
        reader = r
        display = p.createVirtualDisplay(
            "Selene", side, side, m.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, r.surface, null, handler
        )
        refreshContent()
        Log.d(TAG, "캡처 ${side}x${side} @${m.densityDpi}dpi")
    }

    /** 지금 화면 크기(회전 반영)로 가상 디스플레이 안의 내용 영역을 다시 계산한다. */
    private fun refreshContent() {
        if (side == 0) return
        val m = realMetrics()
        val r = MirrorFit.contentRect(m.widthPixels, m.heightPixels, side)
        if (r != content) {
            content = r
            Log.d(TAG, "내용 영역 $r (화면 ${m.widthPixels}x${m.heightPixels})")
        }
    }

    private fun onImage(r: ImageReader) {
        val img = r.acquireLatestImage() ?: return
        try {
            val now = SystemClock.elapsedRealtimeNanos()
            if (now - lastSentNs < FRAME_INTERVAL_NS) return
            val c = content
            if (c.w <= 0 || c.h <= 0) return
            val plane = img.planes[0]
            val paddedW = plane.rowStride / plane.pixelStride
            var bmp = reusable
            if (bmp == null || bmp.width != paddedW || bmp.height != img.height) {
                bmp = Bitmap.createBitmap(paddedW, img.height, Bitmap.Config.ARGB_8888)
                reusable = bmp
            }
            val t0 = System.nanoTime()
            bmp.copyPixelsFromBuffer(plane.buffer)
            // 가운데 맞춰진 폰 화면 영역만 잘라낸다(회전 중 한두 프레임은 어긋날 수 있어 경계를 넘지 않게만 한다)
            val cw = min(c.w, bmp.width - c.x)
            val ch = min(c.h, bmp.height - c.y)
            val cropped = Bitmap.createBitmap(bmp, c.x, c.y, cw, ch)
            val t1 = System.nanoTime()
            jpegOut.reset()
            cropped.compress(Bitmap.CompressFormat.JPEG, jpegQuality, jpegOut)
            cropped.recycle()
            val jpeg = jpegOut.toByteArray()
            val t2 = System.nanoTime()
            link.publishFrame(jpeg)
            val t3 = System.nanoTime()
            lastSentNs = now
            fpsCount++
            copyNs += t1 - t0
            compressNs += t2 - t1
            sendNs += t3 - t2
            if (now - fpsWindowNs >= 1_000_000_000L) {
                val fps = fpsCount
                // 1초마다 단계별 평균(ms): 어디가 병목인지 실기기에서 바로 읽는다
                Log.d(TAG, "${fps}fps 복사·크롭 ${copyNs / fps / 1_000_000}ms 압축 ${compressNs / fps / 1_000_000}ms 전송 ${sendNs / fps / 1_000_000}ms (${jpeg.size / 1024}KB ${cw}x${ch} q$jpegQuality)")
                fpsCount = 0
                copyNs = 0; compressNs = 0; sendNs = 0
                fpsWindowNs = now
                _status.update { it.copy(fps = fps, frame = "${cw}×${ch} · ${jpeg.size / 1024} KB") }
            }
        } catch (e: Exception) {
            Log.w(TAG, "프레임 처리 실패", e)
        } finally {
            img.close()
        }
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
            .setContentText("Selene 가 폰 화면을 차 화면에 보여 줍니다")
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

    /** 공유가 사용자 뜻과 상관없이 끝났을 때(공유 앱 종료 등). 탭하면 앱이 바로 동의 화면을 연다. */
    private fun notifyEnded() {
        val open = PendingIntent.getActivity(
            this, 1,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(EXTRA_RESTART, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("화면 공유가 끝났어요")
            .setContentText("공유하던 앱이 닫혔거나 시스템이 공유를 멈췄습니다. 탭하면 다시 시작합니다")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ENDED_ID, n)
    }

    companion object {
        private const val TAG = "MirrorService"
        private const val CHANNEL = "mirror"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_ENDED_ID = 2
        private const val FRAME_INTERVAL_NS = 1_000_000_000L / Protocol.FRAME_MAX_FPS

        /** JPEG 품질. 65 → 60(11/09): 크기·압축 시간을 조금 줄인다(차 모니터에서 차이 안 보임). 디버그 리시버가 fps 측정용으로 바꾼다 */
        @Volatile var jpegQuality = 60
        /** 가상 디스플레이 한 변의 상한(px). 다음 공유 시작부터 적용. 디버그 리시버가 fps 측정용으로 바꾼다 */
        @Volatile var maxSide = Protocol.FRAME_MAX_SIDE

        const val ACTION_START = "com.hdhollow.studio.phone.START"
        const val ACTION_STOP = "com.hdhollow.studio.phone.STOP"
        const val ACTION_WARMER = "com.hdhollow.studio.phone.WARMER"
        const val ACTION_COOLER = "com.hdhollow.studio.phone.COOLER"
        const val ACTION_ATTACH_USB = "com.hdhollow.studio.phone.ATTACH_USB"
        const val EXTRA_RESTART = "restart" // '공유가 끝났어요' 알림 → 액티비티가 바로 동의 화면을 연다

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

        private val _studio = MutableStateFlow(StudioState())
        /** 차와 같이 보는 앱 상태(앰비언트 연동·풀 라이트·다이얼 대상·차 테마·사용자 프리셋). 차가 기준이고 폰은 마지막으로 받은 값을 들고 있다. */
        val studio: StateFlow<StudioState> = _studio.asStateFlow()

        private val commands = MutableSharedFlow<StudioCommand>(extraBufferCapacity = 16)

        /** 차가 붙어 있는가. 앱 상태·프리셋 저장은 그때만 바꿀 수 있다. */
        val linked: Boolean get() = _status.value.clients > 0

        /** 폰에서 앱 상태를 바꾼다. 차가 붙어 있을 때만(떨어져 있으면 바꿀 대상이 없다). */
        fun updateStudio(change: (StudioState) -> StudioState) {
            if (linked) _studio.update { change(it).copy(src = Protocol.ROLE_PHONE) }
        }

        /** 프리셋 적용. 조명 값은 폰에서 바로 바꾸고(차로 동기화), 차가 붙어 있으면 차 쪽 처리(앰비언트 연동·실내등)도 부탁한다. */
        fun applyScene(scene: Scene) {
            updateLighting { Scenes.apply(scene, it) }
            if (linked) commands.tryEmit(StudioCommand(StudioCommand.SCENE_APPLY, scene.id))
        }

        /** 지금 값을 새 프리셋으로(차가 저장). */
        fun saveScene() {
            if (linked) commands.tryEmit(StudioCommand(StudioCommand.SCENE_SAVE))
        }

        fun deleteScene(scene: Scene) {
            if (linked) commands.tryEmit(StudioCommand(StudioCommand.SCENE_DELETE, scene.id))
        }

        fun startIntent(context: Context, resultCode: Int, data: Intent): Intent =
            Intent(context, MirrorService::class.java).setAction(ACTION_START)
                .putExtra(EXTRA_RESULT_CODE, resultCode).putExtra(EXTRA_RESULT_DATA, data)

        fun stopIntent(context: Context): Intent = Intent(context, MirrorService::class.java).setAction(ACTION_STOP)

        private var prefs: SharedPreferences? = null
        private val loaded = AtomicBoolean(false)
        private const val KEY_LAST = "last"

        /** 마지막 조명 값을 불러온다(액티비티·서비스 onCreate 에서. 한 번만). 기본값(ts 0)은 의미가 없어 건너뛴다. */
        fun init(context: Context) {
            if (!loaded.compareAndSet(false, true)) return
            val p = context.applicationContext.getSharedPreferences("lighting", Context.MODE_PRIVATE)
            prefs = p
            p.getString(KEY_LAST, null)?.let(LightingParams::fromJson)?.takeIf { it.ts > 0 }?.let { _lighting.value = it }
        }

        private fun setLighting(p: LightingParams) {
            _lighting.value = p
            if (p.ts > 0) prefs?.edit()?.putString(KEY_LAST, p.toJson())?.apply()
        }

        /** 폰에서 조명 값을 바꾼다. ts 는 바꾼 시각(어느 쪽이 최신인지 가리는 기준). 서비스가 떠 있으면 차로 보낸다. */
        fun updateLighting(change: (LightingParams) -> LightingParams) {
            setLighting(change(_lighting.value).normalized().copy(src = Protocol.ROLE_PHONE, ts = System.currentTimeMillis()))
        }

        /** 차에서 온 값. 내 값보다 최신일 때만 받는다(LightingParams.supersedes). 받은 값은 src=car 라 되돌려 보내지 않는다. */
        private fun acceptFromCar(p: LightingParams) {
            if (p.supersedes(_lighting.value)) setLighting(p)
        }
    }
}
