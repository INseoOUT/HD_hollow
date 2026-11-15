package com.hdhollow.studio.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.hdhollow.studio.shared.StudioState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioPaletteTest {

    private fun contrast(a: Color, b: Color): Float {
        val (hi, lo) = if (a.luminance() > b.luminance()) a.luminance() to b.luminance() else b.luminance() to a.luminance()
        return (hi + 0.05f) / (lo + 0.05f)
    }

    private fun chroma(c: Color): Float = maxOf(c.red, c.green, c.blue) - minOf(c.red, c.green, c.blue)

    private val car = StudioState.THEMES.map(CarThemes::palette)
    private val all = car + StudioPalettes.Light

    @Test
    fun `차 테마는 계약의 id 마다 하나씩 있고 이름이 서로 다르다`() {
        assertEquals(StudioState.THEMES, CarThemes.ids)
        assertEquals(car.size, car.toSet().size)
        assertEquals(CarThemes.ids.size, CarThemes.ids.map(CarThemes::label).toSet().size)
        assertEquals(StudioPalettes.Black, CarThemes.palette("모르는 값"))
    }

    @Test
    fun `차 테마는 모두 어둡고 채도가 낮다`() {
        car.forEach { p ->
            assertFalse(p.light)
            listOf(p.ink, p.surface, p.surfaceHigh, p.line).forEach { c ->
                assertTrue("차 화면 바탕은 빛을 내면 안 된다: $c", c.luminance() < 0.05f)
                assertTrue("바탕은 색조만 살짝: $c", chroma(c) < 0.12f)
            }
        }
    }

    @Test
    fun `모든 테마에서 글자가 읽힌다`() {
        all.forEach { p ->
            listOf(p.ink, p.surface, p.surfaceHigh).forEach { bg ->
                assertTrue("본문 글자 ${p.text} on $bg", contrast(p.text, bg) >= 7f)
                assertTrue("흐린 글자 ${p.textDim} on $bg", contrast(p.textDim, bg) >= 4.5f)
            }
            listOf(p.ink, p.surface).forEach { bg ->
                assertTrue("가장 흐린 글자 ${p.textFaint} on $bg", contrast(p.textFaint, bg) >= 3f)
                assertTrue("상태 색 ${p.ok} on $bg", contrast(p.ok, bg) >= 3f)
                assertTrue("삭제·오류 색 ${p.danger} on $bg", contrast(p.danger, bg) >= 4.5f)
            }
            assertTrue("선택된 칩(글자색 바탕에 바탕색 글자)", contrast(p.ink, p.text) >= 7f)
        }
    }

    @Test
    fun `폰 라이트 모드는 밝은 바탕이다`() {
        assertTrue(StudioPalettes.Light.light)
        assertTrue(StudioPalettes.Light.ink.luminance() > 0.8f)
    }
}
