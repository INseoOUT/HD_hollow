package com.hdhollow.studio.shared

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 켈빈 → ARGB. 흑체 복사 근사식(Tanner Helland, 1000~40000K). 링라이트·폰 미리보기·앰비언트가 같은 함수를 쓴다.
 * 우리 범위(2700~8000K)에서 2700K ≈ (255,167,87), 4000K ≈ (255,206,166), 6500K ≈ (255,254,250),
 * 7000K ≈ (243,242,255)(기본값, 푸른기 도는 흰색), 8000K ≈ (221,230,255).
 */
object ColorTemp {
    /** 무드 색. 키는 [LightingParams.PRESETS] 와 같다. */
    val MOOD_COLORS: Map<String, Int> = linkedMapOf(
        "cyan" to 0xFF00E5FF.toInt(),
        "blue" to 0xFF2F6BFF.toInt(),
        "purple" to 0xFFB04DFF.toInt(),
    )

    fun kelvinToArgb(kelvin: Int): Int {
        val t = kelvin.coerceIn(LightingParams.KELVIN_MIN, LightingParams.KELVIN_MAX) / 100.0
        val r = if (t <= 66) 255.0 else 329.698727446 * (t - 60).pow(-0.1332047592)
        val g = if (t <= 66) 99.4708025861 * ln(t) - 161.1195681661 else 288.1221695283 * (t - 60).pow(-0.0755148492)
        val b = when {
            t >= 66 -> 255.0
            t <= 19 -> 0.0
            else -> 138.5177312231 * ln(t - 10) - 305.0447927307
        }
        return argb(255, channel(r), channel(g), channel(b))
    }

    /** preset 이 있으면 무드 색, 없으면 색온도. 링 색과 앰비언트에 같은 값을 넣는다. */
    fun colorFor(params: LightingParams): Int =
        params.preset?.let { MOOD_COLORS[it] } ?: kelvinToArgb(params.kelvin)

    fun red(argb: Int): Int = (argb shr 16) and 0xFF
    fun green(argb: Int): Int = (argb shr 8) and 0xFF
    fun blue(argb: Int): Int = argb and 0xFF

    private fun channel(v: Double): Int = v.roundToInt().coerceIn(0, 255)

    private fun argb(a: Int, r: Int, g: Int, b: Int): Int =
        (a shl 24) or (r shl 16) or (g shl 8) or b
}
