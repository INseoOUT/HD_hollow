package com.hdhollow.studio.shared

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class MessageCodecTest {

    @Test
    fun `네 종류 메시지가 왕복으로 같다`() {
        val out = ByteArrayOutputStream()
        val hello = Hello(Protocol.ROLE_PHONE, 1080, 2400).toJson().toByteArray()
        val frame = ByteArray(50_000) { (it % 251).toByte() }
        val light = LightingParams(kelvin = 3500, level = 0.7f).toJson().toByteArray()
        MessageCodec.write(out, Protocol.TYPE_HELLO, hello)
        MessageCodec.write(out, Protocol.TYPE_FRAME, frame)
        MessageCodec.write(out, Protocol.TYPE_LIGHT, light)
        MessageCodec.write(out, Protocol.TYPE_PING, ByteArray(0))

        val input = ByteArrayInputStream(out.toByteArray())
        val m1 = MessageCodec.read(input)!!
        assertEquals(Protocol.TYPE_HELLO, m1.type); assertArrayEquals(hello, m1.payload)
        val m2 = MessageCodec.read(input)!!
        assertEquals(Protocol.TYPE_FRAME, m2.type); assertArrayEquals(frame, m2.payload)
        val m3 = MessageCodec.read(input)!!
        assertEquals(Protocol.TYPE_LIGHT, m3.type); assertArrayEquals(light, m3.payload)
        val m4 = MessageCodec.read(input)!!
        assertEquals(Protocol.TYPE_PING, m4.type); assertEquals(0, m4.payload.size)
        assertNull("스트림 끝이면 null", MessageCodec.read(input))
    }

    @Test
    fun `헤더는 type 1바이트 length 4바이트 big-endian`() {
        val bytes = MessageCodec.encode(Protocol.TYPE_LIGHT, byteArrayOf(1, 2, 3))
        assertEquals(MessageCodec.HEADER_BYTES + 3, bytes.size)
        assertEquals(Protocol.TYPE_LIGHT, bytes[0].toInt())
        assertArrayEquals(byteArrayOf(0, 0, 0, 3), bytes.copyOfRange(1, 5))
    }

    @Test
    fun `상한을 넘는 payload 는 쓰기에서 거부`() {
        assertThrows(IOException::class.java) {
            MessageCodec.write(ByteArrayOutputStream(), Protocol.TYPE_LIGHT, ByteArray(Protocol.LIGHT_MAX + 1))
        }
        assertThrows(IOException::class.java) {
            MessageCodec.write(ByteArrayOutputStream(), Protocol.TYPE_FRAME, ByteArray(Protocol.FRAME_MAX + 1))
        }
        // 상한 자체는 된다
        MessageCodec.write(ByteArrayOutputStream(), Protocol.TYPE_LIGHT, ByteArray(Protocol.LIGHT_MAX))
    }

    @Test
    fun `상한을 넘는 길이는 읽기에서 거부`() {
        val bad = byteArrayOf(Protocol.TYPE_LIGHT.toByte(), 0x00, 0x10, 0x00, 0x00) // 1 MiB 라고 주장
        assertThrows(IOException::class.java) { MessageCodec.read(ByteArrayInputStream(bad)) }
        val negative = byteArrayOf(Protocol.TYPE_PING.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        assertThrows(IOException::class.java) { MessageCodec.read(ByteArrayInputStream(negative)) }
    }

    @Test
    fun `도중에 끊기면 IOException`() {
        val full = MessageCodec.encode(Protocol.TYPE_FRAME, ByteArray(100))
        assertThrows(IOException::class.java) { MessageCodec.read(ByteArrayInputStream(full.copyOf(3))) } // 헤더 도중
        assertThrows(IOException::class.java) { MessageCodec.read(ByteArrayInputStream(full.copyOf(60))) } // payload 도중
    }

    @Test
    fun `Hello 왕복과 버전`() {
        val h = Hello(Protocol.ROLE_CAR, 1680, 1210, now = 1731000000000L)
        assertEquals(h, Hello.fromJson(h.toJson()))
        assertEquals(Protocol.VERSION, h.v)
        // now 가 없는 옛 HELLO 는 0(보정 없음)
        assertEquals(0L, Hello.fromJson("""{"v":1,"role":"phone","screen":[1080,2400]}""")!!.now)
        assertNull(Hello.fromJson("{"))
    }
}
