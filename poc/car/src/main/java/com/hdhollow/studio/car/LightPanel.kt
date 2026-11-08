package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.constant.control.LightSwitch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import kotlin.math.roundToInt

/** 오른쪽 조명 패널 폭. 앱 영역(약 1680px, 160dpi)에서 세로 폰 화면(약 560px) 옆에 들어간다. */
val PANEL_WIDTH = 380.dp

private val PanelBg = Color(0xE614161A)
private val Dim = Color(0xFF9A9FA8)
private val Line = Color(0xFF2C3038)

/**
 * 조명 패널: 링라이트(밝기·색온도·무드 색·다이얼 대상), 앰비언트(연동·켜짐·현재 값), 실내등, 좌석별 독서등.
 * capability 가 없는 섹션은 보이지 않는다.
 */
@Composable
fun LightPanel(light: StudioLight, lighting: Lighting, studio: StudioVehicle, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .width(PANEL_WIDTH)
            .background(PanelBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Section("링라이트") {
            LabelRow("밝기", "${(light.level * 100).roundToInt()}%")
            Slider(light.level, { light.level = it }, Modifier.fillMaxWidth())
            LabelRow("색온도", light.colorLabel)
            Slider(
                light.kelvin.toFloat(),
                { light.chooseKelvin((it / LightingParams.KELVIN_SLIDER_STEP).roundToInt() * LightingParams.KELVIN_SLIDER_STEP) },
                Modifier.fillMaxWidth(),
                valueRange = LightingParams.KELVIN_MIN.toFloat()..LightingParams.KELVIN_MAX.toFloat(),
                steps = (LightingParams.KELVIN_MAX - LightingParams.KELVIN_MIN) / LightingParams.KELVIN_SLIDER_STEP - 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("무드 색", color = Dim, fontSize = 13.sp)
                LightingParams.PRESETS.forEach { name ->
                    val c = Color(ColorTemp.MOOD_COLORS.getValue(name))
                    Box(
                        Modifier.size(32.dp).background(c, CircleShape)
                            .border(if (light.preset == name) 3.dp else 0.dp, Color.White, CircleShape)
                            .clickable { light.choosePreset(if (light.preset == name) null else name) }
                    )
                }
            }
            // 에뮬레이터에선 다이얼 누름·nudge가 앱에 안 와서(RotaryService가 소비) 터치로 전환
            Chip("다이얼: ${if (light.dialColor) "색온도" else "밝기"} ⇄", selected = false) { light.dialColor = !light.dialColor }
        }

        if (lighting.ambient.isNotEmpty()) {
            Section("앰비언트") {
                ToggleRow("링라이트와 연동", light.ambientLinked) { light.ambientLinked = it }
                lighting.ambient.forEach { (area, a) ->
                    if (a.switchOptions.isNotEmpty()) {
                        SwitchRow(area.label(), a.switch, a.switchOptions) { studio.setAmbientSwitch(area, it) }
                    }
                    Text(
                        "${area.label()} · 밝기 ${a.brightness ?: "?"} · 색 ${a.color?.let { "#%06X".format(it and 0xFFFFFF) } ?: "?"}",
                        color = Dim, fontSize = 12.sp,
                    )
                }
            }
        }

        if (lighting.hasCabin) {
            Section("실내등") {
                SwitchRow("실내등", lighting.cabin, lighting.cabinOptions) { studio.setCabin(it) }
            }
        }

        if (lighting.hasReading) {
            Section("독서등") {
                lighting.readingOptions.forEach { (area, options) ->
                    SwitchRow(area.label(), lighting.reading[area], options) { studio.setReading(area, it) }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = Color.White, fontSize = 15.sp)
        Box(Modifier.fillMaxWidth().padding(bottom = 2.dp).background(Line).size(1.dp))
        content()
    }
}

@Composable
private fun LabelRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Dim, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Switch(checked, onChange)
    }
}

/** 스위치 값 선택: capability 가 돌려준 옵션만 칩으로 보인다. */
@Composable
private fun SwitchRow(label: String, current: LightSwitch?, options: List<LightSwitch>, onSelect: (LightSwitch) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { opt -> Chip(opt.label(), selected = current == opt) { onSelect(opt) } }
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        color = if (selected) Color.Black else Color.White,
        fontSize = 13.sp,
        modifier = Modifier
            .background(if (selected) Color.White else Color(0xFF2A2E36), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

fun LightSwitch.label(): String = when (this) {
    LightSwitch.OFF -> "끔"
    LightSwitch.ON -> "켬"
    LightSwitch.AUTOMATIC -> "자동"
    LightSwitch.DAYTIME_RUNNING -> "주간"
}
