package com.hdhollow.studio.car

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.hdhollow.studio.shared.Hello
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.LinkSession
import com.hdhollow.studio.shared.Protocol
import com.hdhollow.studio.shared.Transport
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 폰에 붙는 쪽. 연결 하나로 FRAME(폰→차)과 LIGHT(양방향)를 받는다. plan/work-plan.md 2절.
 * 전송 계층은 [connect] 가 만든다(TCP 또는 USB 액세서리). [run] 이 살아 있는 동안 끊기면
 * [Protocol.RECONNECT_DELAY_MS] 뒤 다시 연다. 취소되면 끊는다.
 */
class StudioLinkClient(private val screenW: Int, private val screenH: Int) {

    private val _frames = MutableStateFlow<Bitmap?>(null)
    val frames: StateFlow<Bitmap?> = _frames.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _incomingLight = MutableSharedFlow<LightingParams>(extraBufferCapacity = 16)
    val incomingLight: SharedFlow<LightingParams> = _incomingLight.asSharedFlow()

    @Volatile private var session: LinkSession? = null

    /** 연결 유지 루프. [connect] 가 null 을 주면 못 연 것이라 잠시 뒤 다시 시도한다. */
    suspend fun run(connect: suspend () -> Transport?) {
        try {
            while (true) {
                val transport = connect()
                if (transport == null) {
                    delay(Protocol.RECONNECT_DELAY_MS)
                    continue
                }
                serve(transport)
                _frames.value = null
                _connected.value = false
                delay(Protocol.RECONNECT_DELAY_MS)
            }
        } finally {
            session?.close("차량 앱이 연결을 닫음")
            session = null
            _frames.value = null
            _connected.value = false
        }
    }

    /** 차에서 바뀐 값을 폰에. 연결이 없으면 false. */
    fun sendLight(params: LightingParams): Boolean =
        session?.sendLight(params.copy(src = Protocol.ROLE_CAR, ts = System.currentTimeMillis())) ?: false

    /** 세션 하나를 끝날 때까지 돌린다. */
    private suspend fun serve(transport: Transport) = suspendCancellableCoroutine { cont ->
        lateinit var s: LinkSession
        s = LinkSession(transport, Hello(Protocol.ROLE_CAR, screenW, screenH), object : LinkSession.Listener {
            override fun onHello(peer: Hello) {
                Log.d(TAG, "폰 연결됨 ${peer.screenW}x${peer.screenH} (${transport.name})")
                _connected.value = true
            }

            override fun onFrame(jpeg: ByteArray) {
                val bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, decodeOptions) ?: return
                _frames.value = bmp
            }

            override fun onLight(params: LightingParams) {
                _incomingLight.tryEmit(params)
            }

            override fun onClosed(reason: String) {
                Log.d(TAG, "연결 종료: $reason")
                if (session === s) session = null
                if (cont.isActive) cont.resume(Unit)
            }
        })
        session = s
        s.start()
        cont.invokeOnCancellation { s.close("취소") }
    }

    companion object {
        private const val TAG = "StudioLink"

        // 프레임마다 Bitmap 을 새로 만들므로 메모리를 반으로(RGB_565). 화면 표시용이라 충분하다
        private val decodeOptions = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
    }
}
