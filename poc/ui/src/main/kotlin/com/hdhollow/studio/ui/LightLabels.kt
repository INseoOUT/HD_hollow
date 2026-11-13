package com.hdhollow.studio.ui

import com.hdhollow.studio.shared.LightingParams
import kotlin.math.roundToInt

/** 조명 값을 사람 말로. 차 패널·폰 화면·플로팅 컨트롤이 같은 문구를 쓴다. */
object LightLabels {
    /** 무드 색 이름. 색상환·헥사로 고른 색("#RRGGBB")과 모르는 키는 그대로. */
    fun mood(name: String): String = when (name) {
        "cyan" -> "시안"
        "blue" -> "블루"
        "purple" -> "퍼플"
        else -> name
    }

    /** "7000K" 또는 "퍼플". */
    fun color(p: LightingParams): String = p.preset?.let(::mood) ?: "${p.kelvin}K"

    /** 흰 빛 톤 칩의 이름([LightingParams.WHITE_TONES]). */
    fun whiteTone(kelvin: Int): String = when {
        kelvin < 4500 -> "따뜻한 흰색"
        kelvin < 6600 -> "자연광"
        else -> "푸른 흰색"
    }

    fun percent(level: Float): String = "${(level * 100).roundToInt()}%"

    /** "7000K · 60%". */
    fun summary(p: LightingParams): String = "${color(p)} · ${percent(p.level)}"

    /** 색의 느낌. 색온도 구간 이름 또는 "시안 무드". 기본값 7000K 는 "푸른기 도는 흰색". */
    fun tone(p: LightingParams): String = p.preset?.let { if (it.startsWith("#")) "무드 색" else "${mood(it)} 무드" } ?: when {
        p.kelvin < 3300 -> "따뜻한 전구색"
        p.kelvin < 4500 -> "따뜻한 흰색"
        p.kelvin < 5600 -> "자연광"
        p.kelvin < 6600 -> "주광색"
        p.kelvin < 7600 -> "푸른기 도는 흰색"
        else -> "차가운 푸른빛"
    }

    /** 다이얼 한 칸이 바꾸는 양. "다이얼 1칸 = 250K" 또는 "다이얼 1칸 = 5%". */
    fun dialStep(dialColor: Boolean): String =
        "다이얼 1칸 = " + if (dialColor) "${LightingParams.KELVIN_DIAL_STEP}K" else "${(LightingParams.LEVEL_DIAL_STEP * 100).roundToInt()}%"
}
