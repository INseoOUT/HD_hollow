package com.hdhollow.studio.phone

import kotlin.math.max
import kotlin.math.min

/**
 * AUTO_MIRROR 가상 디스플레이 안에서 폰 화면이 놓이는 영역. 정사각형 가상 디스플레이에 화면을 비율대로 가운데 맞추므로
 * (가로 화면은 letterbox, 세로 화면은 pillarbox) 회전해도 가상 디스플레이를 다시 만들지 않고 이 영역만 잘라 보낸다.
 * 프레임워크(LogicalDisplay.configureDisplayLocked)와 같은 정수 계산이라 경계가 1px 어긋나지 않는다.
 */
object MirrorFit {
    data class Rect(val x: Int, val y: Int, val w: Int, val h: Int)

    /** 가상 디스플레이 한 변: 화면 긴 변과 상한 중 작은 쪽(작은 화면은 키우지 않는다). */
    fun side(w: Int, h: Int, maxSide: Int): Int = max(2, min(maxSide, max(w, h)))

    /** [w]×[h] 화면이 [side]×[side] 가상 디스플레이 안에서 차지하는 영역. */
    fun contentRect(w: Int, h: Int, side: Int): Rect {
        if (w <= 0 || h <= 0 || side <= 0) return Rect(0, 0, 0, 0)
        val cw: Int
        val ch: Int
        if (h < w) { // 가로 화면: 폭을 꽉 채우고 위아래가 빈다
            cw = side
            ch = h * side / w
        } else { // 세로(또는 정사각) 화면: 높이를 꽉 채우고 좌우가 빈다
            cw = w * side / h
            ch = side
        }
        return Rect((side - cw) / 2, (side - ch) / 2, cw, ch)
    }
}
