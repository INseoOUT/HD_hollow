package com.hdhollow.studio.shared

import org.json.JSONException
import org.json.JSONObject
import kotlin.math.abs

/**
 * 프리셋(씬): 링라이트(색온도 또는 무드 색, 밝기) + 앰비언트 연동을 한 번에. plan/spec.md 3.4절.
 * [preset] 이 있으면 무드 색, 없으면 [kelvin]. 차와 폰이 같은 목록을 보여 주고 같은 값으로 적용한다.
 * 실내등·독서등을 끄는 것은 차량 앱만 한다(기본 프리셋일 때. 폰에서 골라도 차가 처리한다).
 */
data class Scene(
    val id: String,
    val name: String,
    val level: Float,
    val kelvin: Int? = null,
    val preset: String? = null,
    val ambientLinked: Boolean = true,
) {
    val builtIn: Boolean get() = !id.startsWith(USER_PREFIX)

    companion object {
        const val USER_PREFIX = "user-"
    }
}

object Scenes {
    /** 기본 프리셋. 데일리는 기본값과 같은 푸른기 도는 흰색. */
    val BUILT_IN: List<Scene> = listOf(
        Scene("daily", "데일리", level = 0.7f, kelvin = LightingParams.KELVIN_DEFAULT),
        Scene("warm", "웜 뷰티", level = 0.85f, kelvin = 3200),
        Scene("cool", "쿨 시네마", level = 0.45f, kelvin = 8000),
        Scene("neon", "네온", level = 0.6f, preset = "purple"),
    )

    private const val USER_NAME = "내 프리셋 "

    /** 프리셋을 조명 값에 적용한 결과(순수). src·ts 는 호출자가 채운다. */
    fun apply(scene: Scene, p: LightingParams): LightingParams =
        if (scene.preset != null) p.copy(level = scene.level, preset = scene.preset)
        else p.copy(level = scene.level, kelvin = scene.kelvin ?: p.kelvin, preset = null)

    /** 프리셋 칩의 색 점: 무드 색 또는 색온도의 색. 둘 다 없으면 흰색. */
    fun argb(scene: Scene): Int =
        scene.preset?.let(ColorTemp::moodArgb) ?: scene.kelvin?.let(ColorTemp::kelvinToArgb) ?: 0xFFFFFFFF.toInt()

    /** 지금 조명 값과 같은 프리셋(칩 선택 표시용). 없으면 null. */
    fun matching(p: LightingParams, all: List<Scene> = BUILT_IN): Scene? = all.firstOrNull { s ->
        s.preset == p.preset &&
            (s.preset != null || s.kelvin == p.kelvin) &&
            abs(s.level - p.level) < 0.001f
    }

    /** 지금 값으로 사용자 프리셋을 만든다. */
    fun fromCurrent(p: LightingParams, ambientLinked: Boolean, name: String, now: Long = System.currentTimeMillis()): Scene = Scene(
        id = "${Scene.USER_PREFIX}$now",
        name = name,
        level = p.level,
        kelvin = if (p.preset == null) p.kelvin else null,
        preset = p.preset,
        ambientLinked = ambientLinked,
    )

    /** 사용자 프리셋의 자동 이름. 비어 있는 가장 작은 번호("내 프리셋 N"). */
    fun nextUserName(existing: List<Scene>): String {
        val used = existing.mapNotNull { it.name.removePrefix(USER_NAME).toIntOrNull() }.toSet()
        return USER_NAME + generateSequence(1) { it + 1 }.first { it !in used }
    }

    fun toJson(s: Scene): JSONObject = JSONObject()
        .put("id", s.id).put("name", s.name).put("level", s.level.toDouble())
        .put("kelvin", s.kelvin ?: JSONObject.NULL).put("preset", s.preset ?: JSONObject.NULL)
        .put("ambientLinked", s.ambientLinked)

    fun fromJson(o: JSONObject): Scene? = try {
        Scene(
            id = o.getString("id"),
            name = o.getString("name"),
            level = o.getDouble("level").toFloat().coerceIn(0f, 1f),
            kelvin = if (o.isNull("kelvin")) null else o.getInt("kelvin").coerceIn(LightingParams.KELVIN_MIN, LightingParams.KELVIN_MAX),
            preset = if (o.isNull("preset")) null else LightingParams.normalizePreset(o.getString("preset")),
            ambientLinked = o.optBoolean("ambientLinked", true),
        )
    } catch (_: JSONException) {
        null
    }
}
