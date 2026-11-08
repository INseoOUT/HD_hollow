package com.hdhollow.studio.shared

import org.json.JSONException
import org.json.JSONObject

/**
 * 조명 값. 차(다이얼·터치)와 폰(앱 화면·알림)이 같은 값을 공유한다.
 *
 * - [kelvin] 색온도. [preset] 이 null 일 때만 쓴다.
 * - [level] 밝기 0..1.
 * - [preset] 무드 색 이름([PRESETS] 중 하나) 또는 null(색온도 모드).
 * - [src] 보낸 쪽. 자기 것이 돌아오면 무시한다.
 * - [ts] 로그용. 기기 간 시계 차이가 있어 비교에 쓰지 않는다.
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
        preset = preset?.takeIf { it in PRESETS },
    )

    /** 조명에 영향을 주는 값만 비교(src·ts 제외). 되돌아온 자기 값을 걸러낼 때 쓴다. */
    fun sameLight(other: LightingParams): Boolean =
        kelvin == other.kelvin && level == other.level && preset == other.preset

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
        const val KELVIN_SLIDER_STEP = 100 // 슬라이더 단위
        const val KELVIN_NOTIFICATION_STEP = 300 // 폰 알림 버튼 '따뜻하게·차갑게'
        const val LEVEL_DEFAULT = 0.6f
        const val LEVEL_DIAL_STEP = 0.05f
        val PRESETS: List<String> = listOf("cyan", "blue", "purple")

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
