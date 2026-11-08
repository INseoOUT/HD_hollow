package com.hdhollow.studio.shared

import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * 연결 하나의 수명. 폰(서버 쪽)과 차(클라이언트 쪽)가 같은 클래스를 쓴다.
 *
 * - 시작하면 HELLO 를 보내고 읽기 스레드와 PING 스레드를 띄운다.
 * - 받은 메시지는 [Listener] 로 넘긴다. HELLO 의 버전이 다르면 끊는다.
 * - [Protocol.IDLE_TIMEOUT_MS] 동안 아무것도 안 오면 끊는다. 끊김은 [Listener.onClosed] 한 번으로 알린다.
 * - 쓰기는 한 번에 하나(잠금). FRAME 은 [offerFrame] 으로 넣되, 앞 프레임을 아직 쓰는 중이면 버린다(최신 프레임만).
 * - LIGHT 의 ts 는 상대 HELLO 의 now 와 내 시계의 차이만큼 보정해 넘긴다(어느 쪽이 최신인지 가리는 비교용).
 *
 * 외부 의존성 없이 스레드로 만든다(코루틴은 shared 에 없다).
 */
class LinkSession(
    private val transport: Transport,
    private val hello: Hello,
    private val listener: Listener,
    private val now: () -> Long = System::currentTimeMillis,
) {
    interface Listener {
        fun onHello(peer: Hello) {}
        fun onFrame(jpeg: ByteArray) {}
        fun onLight(params: LightingParams) {}
        fun onClosed(reason: String) {}
    }

    private val started = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val writeLock = Any()
    private val frameBusy = AtomicBoolean(false)
    // HELLO·LIGHT 는 호출자가 메인 스레드일 수 있어(NetworkOnMainThreadException) 쓰기 스레드 하나로 보낸다.
    // FRAME 은 캡처 스레드에서 바로 쓴다(막혀도 되는 스레드). 두 경로 모두 writeLock 으로 한 번에 하나만 쓴다
    private val writer: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "link-write ${transport.name}").apply { isDaemon = true }
    }
    @Volatile private var lastRx = now()
    @Volatile var peer: Hello? = null
        private set
    @Volatile private var clockOffset = 0L // 내 시계 - 상대 시계

    val isOpen: Boolean get() = !closed.get()

    fun start() {
        if (!started.compareAndSet(false, true)) return
        lastRx = now()
        enqueue(Protocol.TYPE_HELLO, hello.toJson().toByteArray(Charsets.UTF_8), "hello")
        thread(name = "link-read ${transport.name}", isDaemon = true) { readLoop() }
        thread(name = "link-ping ${transport.name}", isDaemon = true) { pingLoop() }
    }

    /** 쓰기 스레드에 넣는다. 닫혔으면 false(실제 쓰기 실패는 세션 종료로 드러난다). */
    fun sendLight(params: LightingParams): Boolean =
        enqueue(Protocol.TYPE_LIGHT, params.toJson().toByteArray(Charsets.UTF_8), "light")

    private fun enqueue(type: Int, payload: ByteArray, what: String): Boolean {
        if (closed.get()) return false
        return try {
            writer.execute { trySend(type, payload, what) }
            true
        } catch (_: RejectedExecutionException) {
            false
        }
    }

    /** 최신 프레임만. 앞 프레임을 쓰는 중이면 false 를 돌려주고 버린다. */
    fun offerFrame(jpeg: ByteArray): Boolean {
        if (!frameBusy.compareAndSet(false, true)) return false
        try {
            return trySend(Protocol.TYPE_FRAME, jpeg, "frame")
        } finally {
            frameBusy.set(false)
        }
    }

    fun close(reason: String = "closed") {
        if (!closed.compareAndSet(false, true)) return
        writer.shutdownNow()
        runCatching { transport.close() }
        listener.onClosed(reason)
    }

    private fun trySend(type: Int, payload: ByteArray, what: String): Boolean {
        if (closed.get()) return false
        return try {
            synchronized(writeLock) { MessageCodec.write(transport.output, type, payload) }
            true
        } catch (e: IOException) {
            close("$what 쓰기 실패: ${e.message}")
            false
        }
    }

    private fun readLoop() {
        try {
            while (!closed.get()) {
                val m = MessageCodec.read(transport.input) ?: return close("상대가 끊음")
                lastRx = now()
                when (m.type) {
                    Protocol.TYPE_HELLO -> {
                        val h = Hello.fromJson(String(m.payload, Charsets.UTF_8))
                        if (h == null || h.v != Protocol.VERSION) return close("프로토콜 버전 불일치: ${h?.v}")
                        peer = h
                        if (h.now > 0) clockOffset = now() - h.now
                        listener.onHello(h)
                    }
                    Protocol.TYPE_FRAME -> listener.onFrame(m.payload)
                    Protocol.TYPE_LIGHT -> LightingParams.fromJson(String(m.payload, Charsets.UTF_8))
                        ?.let { p -> listener.onLight(if (p.ts > 0) p.copy(ts = p.ts + clockOffset) else p) }
                    Protocol.TYPE_PING -> Unit
                    else -> Unit // 모르는 type 은 버린다(앞으로 호환)
                }
            }
        } catch (e: IOException) {
            close("읽기 실패: ${e.message}")
        } catch (e: RuntimeException) {
            close("리스너 예외: ${e.message}")
        }
    }

    private fun pingLoop() {
        while (!closed.get()) {
            try {
                Thread.sleep(Protocol.PING_INTERVAL_MS)
            } catch (_: InterruptedException) {
                return
            }
            if (closed.get()) return
            if (now() - lastRx > Protocol.IDLE_TIMEOUT_MS) return close("${Protocol.IDLE_TIMEOUT_MS / 1000}초 무수신")
            trySend(Protocol.TYPE_PING, ByteArray(0), "ping")
        }
    }
}
