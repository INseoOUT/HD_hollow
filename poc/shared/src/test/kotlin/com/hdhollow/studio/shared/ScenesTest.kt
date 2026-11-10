package com.hdhollow.studio.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenesTest {

    @Test
    fun `프리셋을 적용하면 조명 값이 바뀌고 같은 프리셋으로 인식된다`() {
        Scenes.BUILT_IN.forEach { scene ->
            val p = Scenes.apply(scene, LightingParams(kelvin = 5000, level = 0.1f, preset = "cyan"))
            assertEquals(scene.level, p.level)
            assertEquals(scene.preset, p.preset)
            if (scene.preset == null) assertEquals(scene.kelvin, p.kelvin)
            assertEquals("적용 직후에는 그 프리셋과 일치해야 한다: ${scene.name}", scene, Scenes.matching(p))
        }
    }

    @Test
    fun `값을 조금만 바꿔도 프리셋과 일치하지 않는다`() {
        val p = Scenes.apply(Scenes.BUILT_IN[0], LightingParams())
        assertNull(Scenes.matching(p.copy(level = p.level + LightingParams.LEVEL_DIAL_STEP)))
        assertNull(Scenes.matching(p.copy(kelvin = p.kelvin - LightingParams.KELVIN_SLIDER_STEP)))
    }

    @Test
    fun `기본 프리셋 id 는 고유하고 무드 색은 알려진 이름만 쓴다`() {
        assertEquals(Scenes.BUILT_IN.size, Scenes.BUILT_IN.map { it.id }.toSet().size)
        Scenes.BUILT_IN.mapNotNull { it.preset }.forEach { assertTrue(it in LightingParams.PRESETS) }
        assertTrue(Scenes.BUILT_IN.all { it.builtIn })
    }

    @Test
    fun `사용자 프리셋은 지금 값을 담고 JSON 왕복이 같다`() {
        val p = LightingParams(kelvin = 5200, level = 0.35f)
        val s = Scenes.fromCurrent(p, ambientLinked = false, name = "내 프리셋 1", now = 42L)
        assertFalse(s.builtIn)
        assertEquals("user-42", s.id)
        assertEquals(5200, s.kelvin)
        assertNull(s.preset)
        assertFalse(s.ambientLinked)
        assertEquals(s, Scenes.fromJson(Scenes.toJson(s)))
        assertEquals(s, Scenes.matching(p, Scenes.BUILT_IN + s))

        val m = Scenes.fromCurrent(p.copy(preset = "cyan"), ambientLinked = true, name = "내 프리셋 2", now = 43L)
        assertEquals("cyan", m.preset)
        assertNull("무드 색 프리셋은 색온도를 저장하지 않는다", m.kelvin)
        assertEquals(m, Scenes.fromJson(Scenes.toJson(m)))
    }

    @Test
    fun `사용자 프리셋 이름은 비어 있는 가장 작은 번호를 쓴다`() {
        fun named(n: Int) = Scene("user-$n", "내 프리셋 $n", 0.5f, kelvin = 5000)
        assertEquals("내 프리셋 1", Scenes.nextUserName(emptyList()))
        assertEquals("내 프리셋 2", Scenes.nextUserName(listOf(named(1), named(3))))
        assertEquals("내 프리셋 4", Scenes.nextUserName(listOf(named(1), named(2), named(3))))
    }

    @Test
    fun `프리셋 칩의 색 점은 무드 색이 있으면 그 색, 없으면 색온도의 색`() {
        assertEquals(ColorTemp.MOOD_COLORS["purple"], Scenes.argb(Scene("x", "네온", 0.6f, preset = "purple")))
        assertEquals(ColorTemp.kelvinToArgb(3200), Scenes.argb(Scene("y", "웜", 0.85f, kelvin = 3200)))
        assertEquals(0xFFFFFFFF.toInt(), Scenes.argb(Scene("z", "빈", 0.5f)))
        Scenes.BUILT_IN.forEach { assertEquals(0xFF, Scenes.argb(it) ushr 24) } // 기본 프리셋은 모두 불투명한 색
    }
}
