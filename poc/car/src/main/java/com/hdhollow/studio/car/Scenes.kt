package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.constant.control.LightSwitch
import android.content.Context
import com.hdhollow.studio.shared.Scene
import com.hdhollow.studio.shared.Scenes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException

/**
 * 프리셋([Scene], 계약은 shared)을 차에 적용한다. 조명 값은 폰에도 동기화되고, 실내등·독서등은 차만 다룬다.
 * 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바꾼다(2026-11-09 결정): 기본 프리셋은 머리 위 그림자를 없애려고 둘 다 끄고,
 * 사용자 프리셋은 건드리지 않는다. 폰에서 프리셋을 골라도 같은 경로로 적용한다(StudioCommand.SCENE_APPLY).
 */
object CarScenes {
    /** 조명 값 쪽 적용(순수). 폰에도 동기화되고 앰비언트 연동이 켜져 있으면 앰비언트도 따라간다. */
    fun applyLight(scene: Scene, light: StudioLight) {
        if (scene.preset != null) light.choosePreset(scene.preset) else scene.kelvin?.let(light::chooseKelvin)
        light.level = scene.level
        light.ambientLinked = scene.ambientLinked
    }

    /** 이 프리셋이 실내등·독서등에 넣는 값. null 이면 건드리지 않는다. */
    fun cabinSwitch(scene: Scene): LightSwitch? = if (scene.builtIn) LightSwitch.OFF else null

    /** 차량 조명 쪽 적용. 설정 가능한(옵션에 있는) 값만 보낸다. */
    fun applyVehicle(scene: Scene, lighting: Lighting, studio: StudioVehicle) {
        val sw = cabinSwitch(scene) ?: return
        if (sw in lighting.cabinOptions) studio.setCabin(sw)
        lighting.readingOptions.forEach { (area, options) -> if (sw in options) studio.setReading(area, sw) }
    }
}

/** 사용자 프리셋 저장소(SharedPreferences, JSON 배열). 추가·삭제만 있고 이름은 자동("내 프리셋 N"). 차가 기준이고 폰에는 STATE 로 보낸다. */
class ScenePresets(context: Context) {
    private val prefs = context.getSharedPreferences("scene_presets", Context.MODE_PRIVATE)
    private val _user = MutableStateFlow(load())
    val user: StateFlow<List<Scene>> = _user.asStateFlow()

    /** 기본 + 사용자. 패널 칩 순서. */
    val all: List<Scene> get() = Scenes.BUILT_IN + _user.value

    fun saveCurrent(light: StudioLight): Scene {
        val scene = Scenes.fromCurrent(light.params(""), light.ambientLinked, Scenes.nextUserName(_user.value))
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
