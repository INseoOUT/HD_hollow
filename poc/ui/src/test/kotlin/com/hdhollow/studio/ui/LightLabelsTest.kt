package com.hdhollow.studio.ui

import com.hdhollow.studio.shared.LightingParams
import org.junit.Assert.assertEquals
import org.junit.Test

class LightLabelsTest {
    @Test
    fun `요약은 색과 밝기를 한 줄로`() {
        assertEquals("7000K · 60%", LightLabels.summary(LightingParams()))
        assertEquals("퍼플 · 85%", LightLabels.summary(LightingParams(preset = "purple", level = 0.85f)))
        assertEquals("3500K", LightLabels.color(LightingParams(kelvin = 3500)))
        assertEquals("블루", LightLabels.color(LightingParams(kelvin = 3500, preset = "blue")))
    }

    @Test
    fun `느낌 이름은 구간 경계에서 바뀌고 기본값은 푸른기 도는 흰색`() {
        assertEquals("푸른기 도는 흰색", LightLabels.tone(LightingParams()))
        assertEquals("따뜻한 전구색", LightLabels.tone(LightingParams(kelvin = 2700)))
        assertEquals("따뜻한 전구색", LightLabels.tone(LightingParams(kelvin = 3200)))
        assertEquals("따뜻한 흰색", LightLabels.tone(LightingParams(kelvin = 3300)))
        assertEquals("자연광", LightLabels.tone(LightingParams(kelvin = 5000)))
        assertEquals("주광색", LightLabels.tone(LightingParams(kelvin = 6500)))
        assertEquals("차가운 푸른빛", LightLabels.tone(LightingParams(kelvin = 8000)))
        assertEquals("시안 무드", LightLabels.tone(LightingParams(preset = "cyan")))
    }

    @Test
    fun `퍼센트는 반올림`() {
        assertEquals("85%", LightLabels.percent(0.85f))
        assertEquals("0%", LightLabels.percent(0f))
        assertEquals("100%", LightLabels.percent(1f))
        assertEquals("모름", LightLabels.mood("모름"))
    }
}
