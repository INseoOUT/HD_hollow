package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.constant.control.LightSwitch
import kotlin.math.abs

/**
 * 조명 씬: 링라이트(색온도 또는 무드 색, 밝기) + 앰비언트 연동 + 실내등·독서등을 한 번에. plan/work-plan.md C12.
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
)

object Scenes {
    val ALL: List<Scene> = listOf(
        Scene("daily", "데일리", level = 0.7f, kelvin = 4500),
        Scene("warm", "웜 뷰티", level = 0.85f, kelvin = 3200),
        Scene("cool", "쿨 시네마", level = 0.45f, kelvin = 6000),
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

    /** 지금 조명 값과 같은 씬(칩 선택 표시용). 없으면 null. */
    fun matching(light: StudioLight): Scene? = ALL.firstOrNull { s ->
        s.preset == light.preset &&
            (s.preset != null || s.kelvin == light.kelvin) &&
            abs(s.level - light.level) < 0.001f
    }
}
