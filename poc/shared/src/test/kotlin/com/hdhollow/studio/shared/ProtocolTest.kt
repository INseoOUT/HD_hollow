package com.hdhollow.studio.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class ProtocolTest {

    @Test
    fun `계약 상수`() {
        assertEquals(8080, Protocol.PORT)
        assertEquals(5_000L, Protocol.PING_INTERVAL_MS)
        assertEquals(15_000L, Protocol.IDLE_TIMEOUT_MS)
        assertEquals(2_000L, Protocol.RECONNECT_DELAY_MS)
        assertEquals(1 shl 20, Protocol.FRAME_MAX)
        assertEquals(4 shl 10, Protocol.LIGHT_MAX)
        assertEquals(1200, Protocol.FRAME_MAX_SIDE)
        assertEquals(15, Protocol.FRAME_MAX_FPS)
    }

    @Test
    fun `type 별 payload 상한`() {
        assertEquals(Protocol.FRAME_MAX, Protocol.maxPayload(Protocol.TYPE_FRAME))
        assertEquals(Protocol.LIGHT_MAX, Protocol.maxPayload(Protocol.TYPE_LIGHT))
        assertEquals(Protocol.SMALL_MAX, Protocol.maxPayload(Protocol.TYPE_HELLO))
        assertEquals(Protocol.SMALL_MAX, Protocol.maxPayload(Protocol.TYPE_PING))
        assertEquals(Protocol.SMALL_MAX, Protocol.maxPayload(Protocol.TYPE_STATUS))
        assertEquals(Protocol.SMALL_MAX, Protocol.maxPayload(0x7F))
    }

    @Test
    fun `type 이름`() {
        assertEquals("HELLO", Protocol.typeName(Protocol.TYPE_HELLO))
        assertEquals("FRAME", Protocol.typeName(Protocol.TYPE_FRAME))
        assertEquals("LIGHT", Protocol.typeName(Protocol.TYPE_LIGHT))
        assertEquals("PING", Protocol.typeName(Protocol.TYPE_PING))
        assertEquals("STATUS", Protocol.typeName(Protocol.TYPE_STATUS))
        assertEquals("0x7f", Protocol.typeName(0x7F))
    }
}
