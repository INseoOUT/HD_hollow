package com.hdhollow.studio.car

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenesTest {

    @Test
    fun `씬을 적용하면 조명 값이 바뀌고 같은 씬으로 인식된다`() {
        val light = StudioLight()
        Scenes.ALL.forEach { scene ->
            Scenes.applyLight(scene, light)
            assertEquals(scene.level, light.level)
            if (scene.preset != null) assertEquals(scene.preset, light.preset) else assertEquals(scene.kelvin, light.kelvin)
            assertEquals("적용 직후에는 그 씬과 일치해야 한다: ${scene.name}", scene, Scenes.matching(light))
        }
    }

    @Test
    fun `값을 조금만 바꿔도 씬과 일치하지 않는다`() {
        val light = StudioLight()
        Scenes.applyLight(Scenes.ALL[0], light)
        light.rotate(+1) // 밝기 한 칸
        assertNull(Scenes.matching(light))
    }

    @Test
    fun `씬 id 는 고유하고 무드 색은 알려진 이름만 쓴다`() {
        assertEquals(Scenes.ALL.size, Scenes.ALL.map { it.id }.toSet().size)
        Scenes.ALL.mapNotNull { it.preset }.forEach { assertTrue(it in com.hdhollow.studio.shared.LightingParams.PRESETS) }
    }
}
