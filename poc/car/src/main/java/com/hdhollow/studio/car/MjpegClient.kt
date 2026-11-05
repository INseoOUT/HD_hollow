package com.hdhollow.studio.car

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.net.HttpURLConnection
import java.net.URL

/** 폰 MJPEG 스트림의 프레임 1장. sentAt은 폰 시계 기준(차·폰 시계 차이만큼 오차). */
class Frame(val bitmap: Bitmap, val sentAt: Long, val receivedAt: Long)

/**
 * 폰(MjpegServer)에 접속해 프레임을 계속 내보낸다. 연결이 끊기면 예외로 끝난다.
 * ponytail: multipart 파서는 Content-Length 헤더만 신뢰하는 최소 구현(우리 서버 전용).
 */
fun mjpegFrames(address: String): Flow<Frame> = flow {
    val conn = URL("http://$address/").openConnection() as HttpURLConnection
    conn.connectTimeout = 3000
    conn.readTimeout = 5000
    try {
        val input = DataInputStream(BufferedInputStream(conn.inputStream, 64 * 1024))
        while (true) {
            var length = -1
            var sentAt = 0L
            // 파트 헤더: 빈 줄이 나올 때까지 읽는다(앞쪽 HTTP 응답 헤더와 boundary 줄은 건너뜀)
            while (true) {
                @Suppress("DEPRECATION") // 문자셋 문제로 deprecated지만 헤더는 ASCII라 무관
                val line = input.readLine() ?: return@flow
                when {
                    line.startsWith("Content-Length:", ignoreCase = true) -> length = line.substringAfter(':').trim().toInt()
                    line.startsWith("X-Timestamp:", ignoreCase = true) -> sentAt = line.substringAfter(':').trim().toLong()
                    line.isEmpty() && length > 0 -> break
                }
            }
            val bytes = ByteArray(length).also { input.readFully(it) }
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: continue
            emit(Frame(bmp, sentAt, System.currentTimeMillis()))
        }
    } finally {
        conn.disconnect()
    }
}.flowOn(Dispatchers.IO)
