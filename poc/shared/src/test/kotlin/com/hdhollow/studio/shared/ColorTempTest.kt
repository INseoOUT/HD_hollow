package com.hdhollow.studio.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorTempTest {

    private fun assertRgbNear(expected: Triple<Int, Int, Int>, argb: Int, tolerance: Int = 2) {
        val actual = Triple(ColorTemp.red(argb), ColorTemp.green(argb), ColorTemp.blue(argb))
        assertTrue(
            "기대 $expected, 실제 $actual (±$tolerance)",
            kotlin.math.abs(expected.first - actual.first) <= tolerance &&
                kotlin.math.abs(expected.second - actual.second) <= tolerance &&
                kotlin.math.abs(expected.third - actual.third) <= tolerance,
        )
        assertEquals("알파는 불투명", 0xFF, (argb ushr 24) and 0xFF)
    }

    @Test
    fun `켈빈 2700 은 따뜻한 주황`() = assertRgbNear(Triple(255, 167, 87), ColorTemp.kelvinToArgb(2700))

    @Test
    fun `켈빈 4000 은 중간 톤`() = assertRgbNear(Triple(255, 206, 166), ColorTemp.kelvinToArgb(4000))

    @Test
    fun `켈빈 6500 은 거의 흰색`() = assertRgbNear(Triple(255, 254, 250), ColorTemp.kelvinToArgb(6500))

    @Test
    fun `범위 밖 입력은 경계값과 같은 색`() {
        assertEquals(ColorTemp.kelvinToArgb(2700), ColorTemp.kelvinToArgb(1000))
        assertEquals(ColorTemp.kelvinToArgb(6500), ColorTemp.kelvinToArgb(9000))
    }

    @Test
    fun `색온도는 올라갈수록 파란 성분이 늘어난다`() {
        var prev = -1
        for (k in 2700..6500 step 100) {
            val b = ColorTemp.blue(ColorTemp.kelvinToArgb(k))
            assertTrue("$k K 에서 blue $b < 이전 $prev", b >= prev)
            prev = b
        }
    }

    @Test
    fun `preset 이 있으면 무드 색 없으면 색온도`() {
        assertEquals(ColorTemp.MOOD_COLORS["purple"], ColorTemp.colorFor(LightingParams(preset = "purple")))
        assertEquals(ColorTemp.kelvinToArgb(3500), ColorTemp.colorFor(LightingParams(kelvin = 3500)))
        assertEquals(LightingParams.PRESETS.toSet(), ColorTemp.MOOD_COLORS.keys)
    }
}
