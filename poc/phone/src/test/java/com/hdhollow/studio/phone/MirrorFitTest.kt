package com.hdhollow.studio.phone

import org.junit.Assert.assertEquals
import org.junit.Test

class MirrorFitTest {

    @Test
    fun `세로 화면은 좌우가 비고 가로 화면은 위아래가 빈다`() {
        assertEquals(MirrorFit.Rect(330, 0, 540, 1200), MirrorFit.contentRect(1080, 2400, 1200))
        assertEquals(MirrorFit.Rect(0, 330, 1200, 540), MirrorFit.contentRect(2400, 1080, 1200))
        assertEquals(MirrorFit.Rect(0, 0, 1200, 1200), MirrorFit.contentRect(1080, 1080, 1200))
    }

    @Test
    fun `영역은 가상 디스플레이를 벗어나지 않는다`() {
        listOf(1080 to 2340, 720 to 1600, 1440 to 3120, 2340 to 1080, 480 to 800).forEach { (w, h) ->
            val side = MirrorFit.side(w, h, 1200)
            val r = MirrorFit.contentRect(w, h, side)
            assert(r.x >= 0 && r.y >= 0 && r.x + r.w <= side && r.y + r.h <= side) { "$w x $h -> $r in $side" }
            assert(r.w == side || r.h == side) { "한 변은 꽉 채운다: $r" }
        }
    }

    @Test
    fun `한 변은 긴 변과 상한 중 작은 쪽`() {
        assertEquals(1200, MirrorFit.side(1080, 2400, 1200))
        assertEquals(800, MirrorFit.side(480, 800, 1200))
        assertEquals(1200, MirrorFit.side(2400, 1080, 1200))
    }

    @Test
    fun `잘못된 입력은 빈 영역`() {
        assertEquals(MirrorFit.Rect(0, 0, 0, 0), MirrorFit.contentRect(0, 100, 1200))
        assertEquals(MirrorFit.Rect(0, 0, 0, 0), MirrorFit.contentRect(100, 100, 0))
    }
}
