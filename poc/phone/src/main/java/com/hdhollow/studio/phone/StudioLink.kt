package com.hdhollow.studio.phone

import android.util.Log
import com.hdhollow.studio.shared.Hello
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.LinkSession
import com.hdhollow.studio.shared.Protocol
import com.hdhollow.studio.shared.ShareStatus
import com.hdhollow.studio.shared.TcpTransport
import com.hdhollow.studio.shared.Transport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.ServerSocket
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

/**
 * 폰 쪽 링크. 차가 접속해 오면(TCP, 2차에는 USB 액세서리도) 세션을 만들고 FRAME 을 흘려보낸다.
 * LIGHT 는 양방향: 차에서 오면 [onLight], 폰에서 바뀌면 [sendLight]. plan/work-plan.md 2절.
 */
class StudioLink(
    private val port: Int = Protocol.PORT,
    private val hello: () -> Hello,
    private val onLight: (LightingParams) -> Unit,
    private val current: () -> LightingParams, // 차가 붙으면 지금 값을 보내 어느 쪽이 최신인지 가리게 한다
    private val status: () -> ShareStatus, // 차가 붙으면 지금 공유 상태(앱 가려짐 여부)도 보낸다
) {
    private val sessions = CopyOnWriteArrayList<LinkSession>()
    private val _clients = MutableStateFlow(0)
    val clients: StateFlow<Int> = _clients.asStateFlow()

    @Volatile private var server: ServerSocket? = null

    fun start() {
        if (server != null) return
        val socket = ServerSocket(port)
        server = socket
        thread(name = "link-accept", isDaemon = true) {
            while (!socket.isClosed) {
                val client = runCatching { socket.accept() }.getOrNull() ?: break
                runCatching { attach(TcpTransport(client)) }
                    .onFailure { Log.w(TAG, "세션 시작 실패", it) }
            }
        }
    }

    /** 전송 계층이 무엇이든 세션 하나로 붙인다(TCP accept, USB 액세서리). */
    fun attach(transport: Transport) {
        lateinit var session: LinkSession
        session = LinkSession(transport, hello(), object : LinkSession.Listener {
            override fun onHello(peer: Hello) {
                Log.d(TAG, "차 연결됨 ${peer.screenW}x${peer.screenH} (${transport.name})")
                session.sendLight(current().copy(src = Protocol.ROLE_PHONE)) // ts 는 바꾼 시각 그대로. 차가 더 최신이면 무시한다
                session.sendStatus(status())
            }

            override fun onLight(params: LightingParams) = onLight.invoke(params)

            override fun onClosed(reason: String) {
                Log.d(TAG, "차 연결 종료: $reason")
                sessions.remove(session)
                _clients.value = sessions.size
            }
        })
        sessions += session
        _clients.value = sessions.size
        session.start()
    }

    fun stop() {
        runCatching { server?.close() }
        server = null
        sessions.forEach { it.close("폰 앱이 송출을 멈춤") }
        sessions.clear()
        _clients.value = 0
    }

    /** 최신 프레임만. 어느 세션이든 받았으면 true. */
    fun publishFrame(jpeg: ByteArray): Boolean {
        var any = false
        sessions.forEach { if (it.offerFrame(jpeg)) any = true }
        return any
    }

    fun sendLight(params: LightingParams) {
        sessions.forEach { it.sendLight(params.copy(src = Protocol.ROLE_PHONE)) }
    }

    /** 공유 상태(앱 가려짐·공유 끝남)를 모든 차에. */
    fun sendStatus(status: ShareStatus) {
        sessions.forEach { it.sendStatus(status) }
    }

    companion object {
        private const val TAG = "StudioLink"
    }
}

