package com.hdhollow.studio.shared

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * 메시지가 흐르는 바이트 스트림. 1차는 TCP(에뮬레이터·USB 테더링·Wi-Fi), 2차는 USB 액세서리 모드.
 * 앱 로직은 어느 쪽인지 모른다.
 */
interface Transport : Closeable {
    val input: InputStream
    val output: OutputStream

    /** 로그용. 예: "tcp 10.0.2.2:8080", "aoa". */
    val name: String
}

class TcpTransport(private val socket: Socket) : Transport {
    init {
        socket.tcpNoDelay = true
    }

    override val input: InputStream = BufferedInputStream(socket.getInputStream(), 64 * 1024)
    override val output: OutputStream = BufferedOutputStream(socket.getOutputStream(), 64 * 1024)
    override val name: String = "tcp ${socket.inetAddress?.hostAddress}:${socket.port}"

    override fun close() {
        runCatching { socket.close() }
    }
}

/** 이미 열린 스트림 한 쌍을 감싼다(USB 액세서리의 ParcelFileDescriptor, 테스트의 파이프). */
class StreamTransport(
    input: InputStream,
    output: OutputStream,
    override val name: String,
    private val onClose: () -> Unit = {},
) : Transport {
    override val input: InputStream = input
    override val output: OutputStream = output

    override fun close() {
        runCatching { input.close() }
        runCatching { output.close() }
        runCatching { onClose() }
    }
}
