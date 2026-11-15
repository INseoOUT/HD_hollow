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
    fun `기본값 7000K 는 푸른기 도는 흰색`() {
        val c = ColorTemp.kelvinToArgb(LightingParams.KELVIN_DEFAULT)
        assertRgbNear(Triple(243, 242, 255), c)
        assertTrue("파랑이 가장 세다", ColorTemp.blue(c) > ColorTemp.red(c) && ColorTemp.blue(c) > ColorTemp.green(c))
        assertRgbNear(Triple(221, 230, 255), ColorTemp.kelvinToArgb(8000))
    }

    @Test
    fun `범위 밖 입력은 경계값과 같은 색`() {
        assertEquals(ColorTemp.kelvinToArgb(2700), ColorTemp.kelvinToArgb(1000))
        assertEquals(ColorTemp.kelvinToArgb(8000), ColorTemp.kelvinToArgb(12000))
    }

    @Test
    fun `색온도는 올라갈수록 파란 성분이 늘고 붉은 성분이 줄어든다`() {
        var prevB = -1
        var prevR = 256
        for (k in 2700..8000 step 100) {
            val c = ColorTemp.kelvinToArgb(k)
            assertTrue("$k K 에서 blue ${ColorTemp.blue(c)} < 이전 $prevB", ColorTemp.blue(c) >= prevB)
            assertTrue("$k K 에서 red ${ColorTemp.red(c)} > 이전 $prevR", ColorTemp.red(c) <= prevR)
            prevB = ColorTemp.blue(c)
            prevR = ColorTemp.red(c)
        }
    }

    @Test
    fun `preset 이 있으면 무드 색 없으면 색온도`() {
        assertEquals(ColorTemp.MOOD_COLORS["purple"], ColorTemp.colorFor(LightingParams(preset = "purple")))
        assertEquals(ColorTemp.kelvinToArgb(3500), ColorTemp.colorFor(LightingParams(kelvin = 3500)))
        assertEquals(LightingParams.PRESETS.toSet(), ColorTemp.MOOD_COLORS.keys)
    }

    @Test
    fun `헥사를 읽고 쓴다`() {
        assertEquals(0xFFFF8040.toInt(), ColorTemp.parseHex("#FF8040"))
        assertEquals(0xFFFF8040.toInt(), ColorTemp.parseHex("ff8040"))
        assertEquals(0xFFFFAA00.toInt(), ColorTemp.parseHex(" #fa0 "))
        assertEquals("#FF8040", ColorTemp.toHex(0xFFFF8040.toInt()))
        assertEquals("#00000A", ColorTemp.toHex(0x0A))
        listOf("", "#", "#12345", "#1234567", "GG0000", "cyan").forEach { assertEquals(it, null, ColorTemp.parseHex(it)) }
    }

    @Test
    fun `무드 색은 이름이나 헥사`() {
        assertEquals(ColorTemp.MOOD_COLORS["cyan"], ColorTemp.moodArgb("cyan"))
        assertEquals(0xFF12AB34.toInt(), ColorTemp.moodArgb("#12AB34"))
        assertEquals(null, ColorTemp.moodArgb("neon"))
        assertEquals(0xFF12AB34.toInt(), ColorTemp.colorFor(LightingParams(kelvin = 3000, preset = "#12AB34")))
    }

    @Test
    fun `색상환 좌표와 색을 오간다`() {
        assertEquals(0xFFFF0000.toInt(), ColorTemp.hsvToArgb(0f, 1f))
        assertEquals(0xFF00FF00.toInt(), ColorTemp.hsvToArgb(120f, 1f))
        assertEquals(0xFF0000FF.toInt(), ColorTemp.hsvToArgb(240f, 1f))
        assertEquals(0xFFFF0000.toInt(), ColorTemp.hsvToArgb(360f, 1f))
        assertEquals("가운데는 흰색", 0xFFFFFFFF.toInt(), ColorTemp.hsvToArgb(200f, 0f))
        assertEquals(0xFF800000.toInt(), ColorTemp.hsvToArgb(0f, 1f, 0.5f))
        for (argb in listOf(0xFFFF8040.toInt(), 0xFF2F6BFF.toInt(), 0xFF123456.toInt(), 0xFF808080.toInt(), 0xFF000000.toInt())) {
            val hsv = ColorTemp.argbToHsv(argb)
            val back = ColorTemp.hsvToArgb(hsv[0], hsv[1], hsv[2])
            listOf(ColorTemp::red, ColorTemp::green, ColorTemp::blue).forEach { ch ->
                assertTrue("${ColorTemp.toHex(argb)} → ${ColorTemp.toHex(back)}", kotlin.math.abs(ch(argb) - ch(back)) <= 1)
            }
        }
    }
}
