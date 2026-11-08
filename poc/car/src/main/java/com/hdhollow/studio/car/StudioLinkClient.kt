package com.hdhollow.studio.car

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.hdhollow.studio.shared.Hello
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.LinkSession
import com.hdhollow.studio.shared.Protocol
import com.hdhollow.studio.shared.TcpTransport
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.coroutines.resume

/**
 * 폰에 접속하는 쪽. 연결 하나로 FRAME(폰→차)과 LIGHT(양방향)를 받는다. plan/work-plan.md 2절.
 * [run] 이 살아 있는 동안 끊기면 [Protocol.RECONNECT_DELAY_MS] 뒤 다시 붙는다. 취소되면 끊는다.
 */
class StudioLinkClient(private val screenW: Int, private val screenH: Int) {

    private val _frames = MutableStateFlow<Bitmap?>(null)
    val frames: StateFlow<Bitmap?> = _frames.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _incomingLight = MutableSharedFlow<LightingParams>(extraBufferCapacity = 16)
    val incomingLight: SharedFlow<LightingParams> = _incomingLight.asSharedFlow()

    @Volatile private var session: LinkSession? = null

    /** 연결 유지 루프. [resolve] 가 null 을 주면 주소를 못 찾은 것이라 잠시 뒤 다시 묻는다. */
    suspend fun run(resolve: suspend () -> InetSocketAddress?) {
        try {
            while (true) {
                val addr = resolve()
                if (addr == null) {
                    delay(Protocol.RECONNECT_DELAY_MS)
                    continue
                }
                connectOnce(addr)
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

    /** 접속해서 끊길 때까지 기다린다. 접속 실패도 그냥 돌아온다(호출자가 재시도). */
    private suspend fun connectOnce(addr: InetSocketAddress) = suspendCancellableCoroutine { cont ->
        var s: LinkSession? = null
        val listener = object : LinkSession.Listener {
            override fun onHello(peer: Hello) {
                Log.d(TAG, "폰 연결됨 ${peer.screenW}x${peer.screenH}")
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
        }
        thread(name = "link-connect", isDaemon = true) {
            try {
                val socket = Socket().apply { connect(addr, CONNECT_TIMEOUT_MS) }
                val created = LinkSession(TcpTransport(socket), Hello(Protocol.ROLE_CAR, screenW, screenH), listener)
                s = created
                session = created
                created.start()
            } catch (e: IOException) {
                Log.d(TAG, "접속 실패 $addr: ${e.message}")
                if (cont.isActive) cont.resume(Unit)
            }
        }
        cont.invokeOnCancellation { s?.close("취소") }
    }

    companion object {
        private const val TAG = "StudioLink"
        private const val CONNECT_TIMEOUT_MS = 3000

        // 프레임마다 Bitmap 을 새로 만들므로 메모리를 반으로(RGB_565). 화면 표시용이라 충분하다
        private val decodeOptions = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
    }
}
