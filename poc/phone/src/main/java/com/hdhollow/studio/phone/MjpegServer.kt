package com.hdhollow.studio.phone

import java.io.BufferedOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * 카메라 JPEG 프레임을 HTTP multipart(MJPEG)로 내보내는 초소형 서버.
 * 차량 앱이 http://<폰IP>:8080/ 으로 접속해 받아 간다(차 → 폰 방향 접속이라 에뮬레이터 NAT를 통과).
 *
 * ponytail: 표준 라이브러리만으로 만든 MJPEG. 대역폭이 H.264의 수 배라 640x480·15fps 수준이 한계.
 * 화질·대역폭이 문제가 되면 RTSP/WebRTC(H.264)로 교체.
 */
class MjpegServer(private val port: Int = 8080) {

    /** 최신 프레임(JPEG 바이트)과 캡처 시각(ms). 클라이언트는 새 프레임만 보낸다. */
    private val latest = AtomicReference<Pair<ByteArray, Long>?>(null)
    val clientCount = AtomicInteger(0)
    @Volatile private var server: ServerSocket? = null

    fun publish(jpeg: ByteArray) = latest.set(jpeg to System.currentTimeMillis())

    fun start() {
        if (server != null) return
        val socket = ServerSocket(port)
        server = socket
        thread(name = "mjpeg-accept", isDaemon = true) {
            while (!socket.isClosed) {
                val client = runCatching { socket.accept() }.getOrNull() ?: break
                thread(name = "mjpeg-client", isDaemon = true) { serve(client) }
            }
        }
    }

    fun stop() {
        server?.close()
        server = null
    }

    private fun serve(client: Socket) {
        clientCount.incrementAndGet()
        try {
            client.tcpNoDelay = true
            val out = BufferedOutputStream(client.getOutputStream())
            out.write(
                ("HTTP/1.0 200 OK\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=frame\r\n\r\n").toByteArray()
            )
            var lastSent: ByteArray? = null
            while (!client.isClosed) {
                val (jpeg, ts) = latest.get() ?: run { Thread.sleep(10); null } ?: continue
                if (jpeg === lastSent) { Thread.sleep(5); continue }
                out.write(
                    ("--frame\r\nContent-Type: image/jpeg\r\n" +
                        "Content-Length: ${jpeg.size}\r\n" +
                        "X-Timestamp: $ts\r\n\r\n").toByteArray()
                )
                out.write(jpeg)
                out.write("\r\n".toByteArray())
                out.flush()
                lastSent = jpeg
            }
        } catch (_: Exception) {
            // 클라이언트 연결 끊김: 정상 종료로 취급
        } finally {
            clientCount.decrementAndGet()
            runCatching { client.close() }
        }
    }
}
