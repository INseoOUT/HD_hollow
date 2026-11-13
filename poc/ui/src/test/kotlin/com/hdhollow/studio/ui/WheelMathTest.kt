package com.hdhollow.studio.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class WheelMathTest {
    @Test
    fun `각도는 색상 거리는 채도`() {
        assertEquals(0f to 1f, WheelMath.hueSat(100f, 0f, 100f))
        val (h, s) = WheelMath.hueSat(0f, 50f, 100f)
        assertEquals(90f, h, 0.01f)
        assertEquals(0.5f, s, 0.001f)
        assertEquals(180f, WheelMath.hueSat(-10f, 0f, 100f).first, 0.01f)
        assertEquals(270f, WheelMath.hueSat(0f, -10f, 100f).first, 0.01f)
    }

    @Test
    fun `원 밖은 가장자리로 가운데는 채도 0`() {
        assertEquals(1f, WheelMath.hueSat(300f, 300f, 100f).second, 0f)
        assertEquals(0f, WheelMath.hueSat(0f, 0f, 100f).second, 0f)
    }

    @Test
    fun `손잡이 위치는 터치 위치로 되돌아온다`() {
        for ((hue, sat) in listOf(30f to 0.8f, 200f to 0.25f, 315f to 1f)) {
            val (x, y) = WheelMath.position(hue, sat, 84f)
            val (h, s) = WheelMath.hueSat(x, y, 84f)
            assertEquals(hue, h, 0.01f)
            assertEquals(sat, s, 0.001f)
        }
    }
}
