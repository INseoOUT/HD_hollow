package com.hdhollow.studio.car

/** 스튜디오 화면 배치의 순수 계산. 테스트 대상. 단위는 dp. */
object PanelLayout {
    /** 조명 패널 한 칸의 폭. 차 글자 크기(22sp 칩)에서 프리셋 칩이 한 줄에 둘씩 들어간다. */
    const val PANEL_DP = 480f

    /** 모니터와 패널 사이, 안쪽 창과 내용 사이 간격. */
    const val GAP_DP = 16f

    /** 패널을 양옆에 두고도 남아야 하는 모니터 폭. 세로 폰 화면(9:20)이 앱 영역 높이(약 980dp)에서 차지하는 폭. */
    const val MONITOR_MIN_DP = 440f

    /** 링라이트 테두리 두께의 범위. 밝기 0 에서도 테두리가 보이고, 최대에서도 안쪽 내용이 들어간다. */
    const val RING_MIN_DP = 8f
    const val RING_MAX_DP = 80f

    /** 링라이트 테두리 두께. 밝기가 올라가면 두꺼워진다(빛나는 면적이 곧 광량). */
    fun ringDp(level: Float): Float = RING_MIN_DP + (RING_MAX_DP - RING_MIN_DP) * level.coerceIn(0f, 1f)

    /**
     * 안쪽 내용(모니터·패널)이 앱 영역 가장자리에서 떨어지는 거리. 테두리가 가장 두꺼울 때를 기준으로 고정한다.
     * 밝기를 바꾸면 테두리만 두꺼워지고 내용은 제자리에 있다(끌고 있는 슬라이더와 모니터 영상이 움직이지 않도록).
     */
    const val CONTENT_INSET_DP = RING_MAX_DP + GAP_DP

    /**
     * 패널을 모니터 양옆 두 칸으로 나눌지.
     * 세로 영상(또는 아직 영상이 없을 때)은 모니터가 좁아도 되므로 양옆에 한 칸씩 두어 스크롤 없이 다 보이게 한다.
     * 가로·정사각 영상은 모니터를 넓게 써야 하므로 오른쪽 한 칸. 앱 영역이 좁을 때도 한 칸.
     * [widthDp] 는 안쪽 내용이 쓸 수 있는 폭, [frameWidth]·[frameHeight] 는 폰에서 온 영상 크기(없으면 null).
     */
    fun twoPanels(widthDp: Float, frameWidth: Int?, frameHeight: Int?): Boolean {
        val portrait = frameWidth == null || frameHeight == null || frameWidth < frameHeight
        return portrait && widthDp >= 2 * PANEL_DP + 2 * GAP_DP + MONITOR_MIN_DP
    }
}
