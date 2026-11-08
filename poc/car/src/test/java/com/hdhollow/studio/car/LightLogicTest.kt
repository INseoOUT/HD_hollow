package com.hdhollow.studio.car

import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LightLogicTest {

    @Test
    fun `색온도 다이얼은 1칸 250K 이고 범위에서 멈춘다`() {
        assertEquals(4250, LightLogic.stepKelvin(4000, +1))
        assertEquals(3500, LightLogic.stepKelvin(4000, -2))
        assertEquals(6500, LightLogic.stepKelvin(6400, +1))
        assertEquals(2700, LightLogic.stepKelvin(2800, -1))
        assertEquals(2700, LightLogic.stepKelvin(2700, -1))
    }

    @Test
    fun `밝기 다이얼은 1칸 0_05 이고 0~1 에서 멈춘다`() {
        assertEquals(0.65f, LightLogic.stepLevel(0.6f, +1))
        assertEquals(0.5f, LightLogic.stepLevel(0.6f, -2))
        assertEquals(1f, LightLogic.stepLevel(0.98f, +1))
        assertEquals(0f, LightLogic.stepLevel(0.02f, -1))
        // 20번 돌려도 오차가 쌓이지 않는다
        var l = 0f
        repeat(20) { l = LightLogic.stepLevel(l, +1) }
        assertEquals(1f, l)
    }

    @Test
    fun `preset 이 없으면 색온도 색 있으면 무드 색`() {
        assertEquals(ColorTemp.kelvinToArgb(3500), LightLogic.argb(LightingParams(kelvin = 3500)))
        assertEquals(ColorTemp.MOOD_COLORS["cyan"], LightLogic.argb(LightingParams(kelvin = 3500, preset = "cyan")))
    }

    @Test
    fun `StudioLight 는 다이얼 대상에 따라 밝기 또는 색온도를 돌리고 무드 색을 푼다`() {
        val light = StudioLight()
        light.rotate(+2)
        assertEquals(0.7f, light.level)
        light.choosePreset("purple")
        light.dialColor = true
        light.rotate(+1)
        assertNull("색온도를 돌리면 무드 색이 풀린다", light.preset)
        assertEquals(4250, light.kelvin)
        light.apply(LightingParams(kelvin = 9000, level = 2f, preset = "neon", src = "phone"))
        assertEquals(6500, light.kelvin)
        assertEquals(1f, light.level)
        assertNull(light.preset)
        assertEquals("6500K", light.colorLabel)
        light.choosePreset("blue")
        assertEquals("블루", light.colorLabel)
        assertEquals(ColorTemp.MOOD_COLORS["blue"], light.argb)
    }
}
