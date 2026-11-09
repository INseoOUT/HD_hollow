package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.constant.control.LightSwitch
import android.content.Context
import com.hdhollow.studio.shared.LightingParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.abs

/**
 * 프리셋(씬): 링라이트(색온도 또는 무드 색, 밝기) + 앰비언트 연동 + 실내등·독서등을 한 번에. plan/work-plan.md C12.
 * 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바꾼다(2026-11-09 결정). [cabin]·[reading] 이 null 이면 건드리지 않는다.
 * [preset] 이 있으면 무드 색, 없으면 [kelvin]. 차량 조명은 capability 옵션에 그 값이 있을 때만 바꾼다.
 */
data class Scene(
    val id: String,
    val name: String,
    val level: Float,
    val kelvin: Int? = null,
    val preset: String? = null,
    val ambientLinked: Boolean = true,
    val cabin: LightSwitch? = LightSwitch.OFF, // 머리 위 그림자를 없애려고 기본은 끔
    val reading: LightSwitch? = LightSwitch.OFF,
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

    /** 조명 값 쪽 적용(순수). 폰에도 동기화되고 앰비언트 연동이 켜져 있으면 앰비언트도 따라간다. */
    fun applyLight(scene: Scene, light: StudioLight) {
        if (scene.preset != null) light.choosePreset(scene.preset) else scene.kelvin?.let(light::chooseKelvin)
        light.level = scene.level
        light.ambientLinked = scene.ambientLinked
    }

    /** 차량 조명 쪽 적용. 설정 가능한(옵션에 있는) 값만 보낸다. */
    fun applyVehicle(scene: Scene, lighting: Lighting, studio: StudioVehicle) {
        scene.cabin?.let { sw -> if (sw in lighting.cabinOptions) studio.setCabin(sw) }
        scene.reading?.let { sw ->
            lighting.readingOptions.forEach { (area, options) -> if (sw in options) studio.setReading(area, sw) }
        }
    }

    /** 지금 조명 값과 같은 프리셋(칩 선택 표시용). 없으면 null. */
    fun matching(light: StudioLight, all: List<Scene> = BUILT_IN): Scene? = all.firstOrNull { s ->
        s.preset == light.preset &&
            (s.preset != null || s.kelvin == light.kelvin) &&
            abs(s.level - light.level) < 0.001f
    }

    /** 지금 값으로 사용자 프리셋을 만든다. 실내등·독서등은 건드리지 않는 프리셋(null). */
    fun fromCurrent(light: StudioLight, name: String, now: Long = System.currentTimeMillis()): Scene = Scene(
        id = "${Scene.USER_PREFIX}$now",
        name = name,
        level = light.level,
        kelvin = if (light.preset == null) light.kelvin else null,
        preset = light.preset,
        ambientLinked = light.ambientLinked,
        cabin = null,
        reading = null,
    )

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
            preset = if (o.isNull("preset")) null else o.getString("preset").takeIf { it in LightingParams.PRESETS },
            ambientLinked = o.optBoolean("ambientLinked", true),
            cabin = null,
            reading = null,
        )
    } catch (_: JSONException) {
        null
    }
}

/** 사용자 프리셋 저장소(SharedPreferences, JSON 배열). 추가·삭제만 있고 이름은 자동("내 프리셋 N"). */
class ScenePresets(context: Context) {
    private val prefs = context.getSharedPreferences("scene_presets", Context.MODE_PRIVATE)
    private val _user = MutableStateFlow(load())
    val user: StateFlow<List<Scene>> = _user.asStateFlow()

    /** 기본 + 사용자. 패널 칩 순서. */
    val all: List<Scene> get() = Scenes.BUILT_IN + _user.value

    fun saveCurrent(light: StudioLight): Scene {
        val used = _user.value.mapNotNull { it.name.removePrefix("내 프리셋 ").toIntOrNull() }.toSet()
        val n = generateSequence(1) { it + 1 }.first { it !in used }
        val scene = Scenes.fromCurrent(light, "내 프리셋 $n")
        _user.value = _user.value + scene
        persist()
        return scene
    }

    fun delete(id: String) {
        _user.value = _user.value.filterNot { it.id == id }
        persist()
    }

    private fun load(): List<Scene> = try {
        val arr = JSONArray(prefs.getString(KEY, "[]") ?: "[]")
        (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(Scenes::fromJson) }
    } catch (_: JSONException) {
        emptyList()
    }

    private fun persist() {
        val arr = JSONArray().also { a -> _user.value.forEach { a.put(Scenes.toJson(it)) } }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    companion object {
        private const val KEY = "user"
    }
}
