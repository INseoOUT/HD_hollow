package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.constant.control.LightSwitch
import com.hdhollow.studio.shared.Scenes
import com.hdhollow.studio.shared.StudioState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenesTest {

    @Test
    fun `프리셋을 적용하면 차의 조명 값이 바뀌고 같은 프리셋으로 인식된다`() {
        val light = StudioLight()
        Scenes.BUILT_IN.forEach { scene ->
            CarScenes.applyLight(scene, light)
            assertEquals(scene.level, light.level)
            if (scene.preset != null) assertEquals(scene.preset, light.preset) else assertEquals(scene.kelvin, light.kelvin)
            assertEquals(scene.ambientLinked, light.ambientLinked)
            assertEquals("적용 직후에는 그 프리셋과 일치해야 한다: ${scene.name}", scene, Scenes.matching(light.params("")))
        }
    }

    @Test
    fun `다이얼로 값을 조금만 바꿔도 프리셋과 일치하지 않는다`() {
        val light = StudioLight()
        CarScenes.applyLight(Scenes.BUILT_IN[0], light)
        light.rotate(+1) // 밝기 한 칸
        assertNull(Scenes.matching(light.params("")))
    }

    @Test
    fun `기본 프리셋은 실내등·독서등을 끄고 사용자 프리셋은 건드리지 않는다`() {
        Scenes.BUILT_IN.forEach { assertEquals(LightSwitch.OFF, CarScenes.cabinSwitch(it)) }
        val light = StudioLight().apply { chooseKelvin(5200); level = 0.35f; ambientLinked = false }
        val mine = Scenes.fromCurrent(light.params(""), light.ambientLinked, "내 프리셋 1", now = 42L)
        assertNull(CarScenes.cabinSwitch(mine))
        assertFalse(mine.ambientLinked)
    }

    @Test
    fun `앱 상태는 조명 객체에서 만들고 폰에서 온 값으로 되돌려 넣을 수 있다`() {
        val light = StudioLight()
        assertEquals(StudioState(), light.state(emptyList()))

        val fromPhone = StudioState(ambientLinked = false, fullLight = true, dialColor = true, theme = StudioState.THEME_NAVY, src = "phone")
        light.apply(fromPhone)
        assertTrue(light.fullLight)
        assertTrue(light.dialColor)
        assertFalse(light.ambientLinked)
        assertEquals(StudioState.THEME_NAVY, light.theme)
        assertTrue("되돌려 보내지 않도록 같은 상태로 인식돼야 한다", light.state(emptyList()).sameState(fromPhone))

        light.apply(fromPhone.copy(theme = "pink"))
        assertEquals("모르는 테마는 기본값", StudioState.THEME_DEFAULT, light.theme)
    }
}
