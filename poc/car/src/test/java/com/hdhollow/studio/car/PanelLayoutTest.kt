package com.hdhollow.studio.car

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PanelLayoutTest {

    /** Pleos 에뮬레이터 2.1.0 의 앱 영역(1680×1208dp)에서 안쪽 내용이 쓰는 폭. */
    private val emulator = 1680f - 2 * PanelLayout.CONTENT_INSET_DP

    @Test
    fun `테두리는 밝기에 따라 8~80dp 이고 범위를 넘지 않는다`() {
        assertEquals(8f, PanelLayout.ringDp(0f))
        assertEquals(80f, PanelLayout.ringDp(1f))
        assertEquals(58.4f, PanelLayout.ringDp(0.7f), 0.01f)
        assertEquals(8f, PanelLayout.ringDp(-1f))
        assertEquals(80f, PanelLayout.ringDp(2f))
    }

    @Test
    fun `안쪽 내용은 테두리가 가장 두꺼울 때도 가려지지 않는다`() {
        assertTrue(PanelLayout.CONTENT_INSET_DP >= PanelLayout.ringDp(1f) + PanelLayout.GAP_DP)
    }

    @Test
    fun `세로 영상이나 영상이 없을 때는 패널을 양옆 두 칸으로`() {
        assertTrue(PanelLayout.twoPanels(emulator, null, null))
        assertTrue(PanelLayout.twoPanels(emulator, 540, 1200))
    }

    @Test
    fun `가로·정사각 영상은 모니터를 넓게 쓰도록 한 칸`() {
        assertFalse(PanelLayout.twoPanels(emulator, 1200, 540))
        assertFalse(PanelLayout.twoPanels(emulator, 800, 800))
    }

    @Test
    fun `앱 영역이 좁으면 세로 영상이어도 한 칸`() {
        val min = 2 * PanelLayout.PANEL_DP + 2 * PanelLayout.GAP_DP + PanelLayout.MONITOR_MIN_DP
        assertTrue(PanelLayout.twoPanels(min, 540, 1200))
        assertFalse(PanelLayout.twoPanels(min - 1f, 540, 1200))
    }

    @Test
    fun `앰비언트 지금 값 문구`() {
        assertEquals("전체 · 밝기 70", AmbientLabel.of("전체", 70))
        assertEquals("운전석 · 밝기 –", AmbientLabel.of("운전석", null))
    }
}
