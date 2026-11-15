package com.hdhollow.studio.shared

import org.json.JSONException
import org.json.JSONObject

/**
 * 조명 값. 차(다이얼·터치)와 폰(앱 화면·알림)이 같은 값을 공유한다.
 *
 * - [kelvin] 색온도. [preset] 이 null 일 때만 쓴다.
 * - [level] 밝기 0..1.
 * - [preset] 무드 색: 색상환·헥사로 고른 "#RRGGBB" 또는 이름([PRESETS] 중 하나). null 이면 색온도 모드.
 * - [src] 보낸 쪽. 자기 것이 돌아오면 무시한다.
 * - [ts] 사용자가 이 값을 마지막으로 바꾼 시각(ms). 0 이면 한 번도 안 바꾼 기본값. 재접속 때 어느 쪽을 따를지 [supersedes] 가 정한다.
 *   기기 간 시계 차이는 [LinkSession] 이 HELLO 의 now 로 받는 쪽 시계에 맞춰 보정한다.
 */
data class LightingParams(
    val kelvin: Int = KELVIN_DEFAULT,
    val level: Float = LEVEL_DEFAULT,
    val preset: String? = null,
    val src: String = Protocol.ROLE_CAR,
    val ts: Long = 0L,
) {
    /** 범위 밖 값을 경계로, 모르는 preset 을 null 로. */
    fun normalized(): LightingParams = copy(
        kelvin = kelvin.coerceIn(KELVIN_MIN, KELVIN_MAX),
        level = level.coerceIn(0f, 1f),
        preset = normalizePreset(preset),
    )

    /** 조명에 영향을 주는 값만 비교(src·ts 제외). 되돌아온 자기 값을 걸러낼 때 쓴다. */
    fun sameLight(other: LightingParams): Boolean =
        kelvin == other.kelvin && level == other.level && preset == other.preset

    /**
     * 재접속·충돌 때 이 값이 [current] 를 대체해야 하는가. 마지막으로 바뀐 쪽(ts 가 큰 쪽)이 기준이고,
     * 같은 시각이면 폰이 보낸 값이 이긴다. 기본값(ts 0)은 바뀐 값에 항상 진다. (2026-11-09 결정)
     */
    fun supersedes(current: LightingParams): Boolean =
        ts > current.ts || (ts == current.ts && src == Protocol.ROLE_PHONE && current.src != Protocol.ROLE_PHONE)

    fun toJson(): String = JSONObject()
        .put("v", Protocol.VERSION)
        .put("kelvin", kelvin)
        .put("level", level.toDouble())
        .put("preset", preset ?: JSONObject.NULL)
        .put("src", src)
        .put("ts", ts)
        .toString()

    companion object {
        const val KELVIN_MIN = 2700
        const val KELVIN_MAX = 8000 // 6500K 위로는 푸른기가 돈다
        const val KELVIN_DEFAULT = 7000 // 기본은 푸른기 도는 흰색: 잡티·피부결이 덜 보인다 (2026-11-09 결정)
        const val KELVIN_DIAL_STEP = 250 // 다이얼 1칸
        /** 화면에서 고르는 흰 빛 톤 셋(따뜻한 흰색·자연광·푸른 흰색). 그 사이 값은 다이얼·알림 버튼으로 간다. */
        val WHITE_TONES: List<Int> = listOf(3500, 5000, KELVIN_DEFAULT)
        const val KELVIN_NOTIFICATION_STEP = 300 // 폰 알림 버튼 '따뜻하게·차갑게'
        const val LEVEL_DEFAULT = 0.6f
        const val LEVEL_DIAL_STEP = 0.05f
        val PRESETS: List<String> = listOf("cyan", "blue", "purple")

        /** 무드 색 값을 표준 꼴로: 이름은 그대로, 헥사는 "#RRGGBB" 대문자로, 그 밖은 null. */
        fun normalizePreset(preset: String?): String? =
            preset?.let { if (it in PRESETS) it else ColorTemp.parseHex(it)?.let(ColorTemp::toHex) }

        /** v 가 다르면 null. 모르는 키는 무시하고 값은 범위로 맞춘다. */
        fun fromJson(text: String): LightingParams? = try {
            val o = JSONObject(text)
            if (o.optInt("v", -1) != Protocol.VERSION) {
                null
            } else {
                LightingParams(
                    kelvin = o.optInt("kelvin", KELVIN_DEFAULT),
                    level = o.optDouble("level", LEVEL_DEFAULT.toDouble()).toFloat(),
                    preset = if (o.isNull("preset")) null else o.optString("preset"),
                    src = o.optString("src", Protocol.ROLE_CAR),
                    ts = o.optLong("ts", 0L),
                ).normalized()
            }
        } catch (_: JSONException) {
            null
        }
    }
}
