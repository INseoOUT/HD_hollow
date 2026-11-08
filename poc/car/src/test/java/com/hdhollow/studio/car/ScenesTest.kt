package com.hdhollow.studio.car

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenesTest {

    @Test
    fun `프리셋을 적용하면 조명 값이 바뀌고 같은 프리셋으로 인식된다`() {
        val light = StudioLight()
        Scenes.BUILT_IN.forEach { scene ->
            Scenes.applyLight(scene, light)
            assertEquals(scene.level, light.level)
            if (scene.preset != null) assertEquals(scene.preset, light.preset) else assertEquals(scene.kelvin, light.kelvin)
            assertEquals("적용 직후에는 그 프리셋과 일치해야 한다: ${scene.name}", scene, Scenes.matching(light))
        }
    }

    @Test
    fun `값을 조금만 바꿔도 프리셋과 일치하지 않는다`() {
        val light = StudioLight()
        Scenes.applyLight(Scenes.BUILT_IN[0], light)
        light.rotate(+1) // 밝기 한 칸
        assertNull(Scenes.matching(light))
    }

    @Test
    fun `기본 프리셋 id 는 고유하고 무드 색은 알려진 이름만 쓴다`() {
        assertEquals(Scenes.BUILT_IN.size, Scenes.BUILT_IN.map { it.id }.toSet().size)
        Scenes.BUILT_IN.mapNotNull { it.preset }.forEach { assertTrue(it in com.hdhollow.studio.shared.LightingParams.PRESETS) }
        assertTrue(Scenes.BUILT_IN.all { it.builtIn })
    }

    @Test
    fun `사용자 프리셋은 지금 값을 담고 JSON 왕복이 같으며 실내등·독서등은 건드리지 않는다`() {
        val light = StudioLight().apply { chooseKelvin(5200); level = 0.35f; ambientLinked = false }
        val s = Scenes.fromCurrent(light, "내 프리셋 1", now = 42L)
        assertFalse(s.builtIn)
        assertEquals("user-42", s.id)
        assertEquals(5200, s.kelvin)
        assertNull(s.preset)
        assertNull(s.cabin)
        assertNull(s.reading)
        assertEquals(s, Scenes.fromJson(Scenes.toJson(s)))
        assertEquals(s, Scenes.matching(light, Scenes.BUILT_IN + s))

        light.choosePreset("cyan")
        val m = Scenes.fromCurrent(light, "내 프리셋 2", now = 43L)
        assertEquals("cyan", m.preset)
        assertNull("무드 색 프리셋은 색온도를 저장하지 않는다", m.kelvin)
        assertEquals(m, Scenes.fromJson(Scenes.toJson(m)))
    }
}
