package com.hdhollow.studio.car

import com.hdhollow.studio.shared.ColorTemp
import org.junit.Assert.assertEquals
import org.junit.Test

class SceneColorTest {
    @Test
    fun `프리셋 칩의 색 점은 무드 색이 있으면 그 색, 없으면 색온도의 색`() {
        assertEquals(ColorTemp.MOOD_COLORS["purple"], Scenes.argb(Scene("x", "네온", 0.6f, preset = "purple")))
        assertEquals(ColorTemp.kelvinToArgb(3200), Scenes.argb(Scene("y", "웜", 0.85f, kelvin = 3200)))
        assertEquals(0xFFFFFFFF.toInt(), Scenes.argb(Scene("z", "빈", 0.5f)))
        Scenes.BUILT_IN.forEach { assertEquals(0xFF, Scenes.argb(it) ushr 24) } // 기본 프리셋은 모두 불투명한 색
    }
}
