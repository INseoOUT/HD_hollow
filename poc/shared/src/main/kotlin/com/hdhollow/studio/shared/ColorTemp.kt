package com.hdhollow.studio.shared

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 켈빈 → ARGB. 흑체 복사 근사식(Tanner Helland, 1000~40000K). 링라이트·폰 미리보기·앰비언트가 같은 함수를 쓴다.
 * 우리 범위(2700~8000K)에서 2700K ≈ (255,167,87), 4000K ≈ (255,206,166), 6500K ≈ (255,254,250),
 * 7000K ≈ (243,242,255)(기본값, 푸른기 도는 흰색), 8000K ≈ (221,230,255).
 */
object ColorTemp {
    /** 이름 있는 무드 색(기본 프리셋 "네온" 등). 키는 [LightingParams.PRESETS] 와 같다. 그 밖의 색은 "#RRGGBB" 로 적는다([moodArgb]). */
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
        params.preset?.let(::moodArgb) ?: kelvinToArgb(params.kelvin)

    /** 무드 색 값(preset)의 색. 이름([MOOD_COLORS]) 또는 "#RRGGBB". 둘 다 아니면 null. */
    fun moodArgb(preset: String): Int? = MOOD_COLORS[preset] ?: parseHex(preset)

    /** "#RRGGBB"·"RRGGBB"·"#RGB"(대소문자 무관) → 불투명 ARGB. 형식이 다르면 null. */
    fun parseHex(text: String): Int? {
        val h = text.trim().removePrefix("#")
        val six = when (h.length) {
            6 -> h
            3 -> h.map { "$it$it" }.joinToString("")
            else -> return null
        }
        if (!six.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
        return 0xFF000000.toInt() or six.toInt(16)
    }

    /** ARGB → "#RRGGBB"(대문자). 무드 색을 주고받는 표기. */
    fun toHex(argb: Int): String = "#%06X".format(argb and 0xFFFFFF)

    /** 색상환 좌표 → 색. [hue] 0..360, [sat]·[value] 0..1. */
    fun hsvToArgb(hue: Float, sat: Float, value: Float = 1f): Int {
        val h = ((hue % 360f) + 360f) % 360f / 60f
        val s = sat.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val c = v * s
        val x = c * (1 - abs(h % 2 - 1))
        val (r, g, b) = when (h.toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val m = v - c
        return argb(255, channel((r + m) * 255.0), channel((g + m) * 255.0), channel((b + m) * 255.0))
    }

    /** 색 → [hue 0..360, sat 0..1, value 0..1]. 무채색의 hue 는 0. */
    fun argbToHsv(argb: Int): FloatArray {
        val r = red(argb) / 255f
        val g = green(argb) / 255f
        val b = blue(argb) / 255f
        val max = maxOf(r, g, b)
        val d = max - minOf(r, g, b)
        val hue = when {
            d == 0f -> 0f
            max == r -> 60f * (((g - b) / d + 6f) % 6f)
            max == g -> 60f * ((b - r) / d + 2f)
            else -> 60f * ((r - g) / d + 4f)
        }
        return floatArrayOf(hue, if (max == 0f) 0f else d / max, max)
    }

    fun red(argb: Int): Int = (argb shr 16) and 0xFF
    fun green(argb: Int): Int = (argb shr 8) and 0xFF
    fun blue(argb: Int): Int = argb and 0xFF

    private fun channel(v: Double): Int = v.roundToInt().coerceIn(0, 255)

    private fun argb(a: Int, r: Int, g: Int, b: Int): Int =
        (a shl 24) or (r shl 16) or (g shl 8) or b
}
