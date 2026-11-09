package com.hdhollow.studio.car

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OriginalsTest {

    @Test
    fun `원래 값은 JSON 왕복이 같다`() {
        val o = Originals(
            ambientBrightness = mapOf("ROW_1_LEFT" to 30, "ROW_1_RIGHT" to 55),
            ambientColor = mapOf("ROW_1_LEFT" to 0xFF00E5FF.toInt()),
            ambientSwitch = mapOf("ROW_1_LEFT" to "ON"),
            cabin = "OFF",
            reading = mapOf("ROW_2_LEFT+ROW_2_RIGHT" to "OFF"),
        )
        assertEquals(o, Originals.fromJson(o.toJson().toString()))
    }

    @Test
    fun `빈 값도 왕복이 같고 isEmpty 다`() {
        val e = Originals()
        assertTrue(e.isEmpty)
        assertEquals(e, Originals.fromJson(e.toJson().toString()))
        assertTrue(Originals(cabin = "ON").isEmpty.not())
    }

    @Test
    fun `깨진 JSON 은 null`() {
        assertNull(Originals.fromJson("not json"))
        assertNull(Originals.fromJson(""))
    }
}
