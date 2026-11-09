package com.hdhollow.studio.car

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
private val Danger = Color(0xFFFF8A7A)

/**
 * 조명 패널: 프리셋(기본 4 + 사용자, 추가·삭제), 링라이트(밝기·색온도·무드 색·다이얼 대상·풀 라이트), 앰비언트(연동·현재 값).
 * 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바뀐다(2026-11-09 결정).
 */
@Composable
fun LightPanel(light: StudioLight, lighting: Lighting, studio: StudioVehicle, presets: ScenePresets, modifier: Modifier = Modifier) {
    val userScenes by presets.user.collectAsState()
    var editing by remember { mutableStateOf(false) } // 삭제 모드: 사용자 프리셋을 탭하면 지운다

    Column(
        modifier
            .fillMaxHeight()
            .width(PANEL_WIDTH)
            .background(PanelBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Section("프리셋") {
            val all = Scenes.BUILT_IN + userScenes
            val current = Scenes.matching(light, all)
            FlowChips(all.map { scene ->
                ChipSpec(
                    text = if (editing && !scene.builtIn) "× ${scene.name}" else scene.name,
                    selected = !editing && current?.id == scene.id,
                    danger = editing && !scene.builtIn,
                    enabled = !editing || !scene.builtIn,
                ) {
                    if (editing) {
                        if (!scene.builtIn) presets.delete(scene.id)
                    } else {
                        Scenes.applyLight(scene, light)
                        Scenes.applyVehicle(scene, lighting, studio)
                    }
                }
            })
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("+ 지금 값 저장", selected = false) { presets.saveCurrent(light); editing = false }
                if (userScenes.isNotEmpty()) Chip(if (editing) "완료" else "삭제", selected = editing) { editing = !editing }
            }
            if (editing) Text("지울 프리셋을 탭하세요. 기본 프리셋은 지울 수 없습니다.", color = Dim, fontSize = 12.sp)
        }

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
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // 에뮬레이터에선 다이얼 누름·nudge가 앱에 안 와서(RotaryService가 소비) 터치로 전환
                Chip("다이얼: ${if (light.dialColor) "색온도" else "밝기"} ⇄", selected = false) { light.dialColor = !light.dialColor }
                // 폰 화면을 직접 봐야 할 때(화상회의): 모니터·패널을 숨기고 전체를 조명으로. 화면 탭으로 복귀
                Chip("풀 라이트", selected = light.fullLight) { light.fullLight = true }
            }
        }

        if (lighting.ambient.isNotEmpty()) {
            Section("앰비언트") {
                ToggleRow("링라이트와 연동", light.ambientLinked) { light.ambientLinked = it }
                lighting.ambient.forEach { (area, a) ->
                    Text(
                        "${area.label()} · 밝기 ${a.brightness ?: "?"} · 색 ${a.color?.let { "#%06X".format(it and 0xFFFFFF) } ?: "?"}" +
                            (a.switch?.let { " · ${it.name.lowercase()}" } ?: ""),
                        color = Dim, fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

private data class ChipSpec(
    val text: String,
    val selected: Boolean,
    val danger: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/** 칩을 패널 폭에 맞춰 줄바꿈한다(글자 수 기준의 단순 배치. 패널 폭 380dp 에 칩 4~5개). */
@Composable
private fun FlowChips(chips: List<ChipSpec>) {
    val rows = mutableListOf<MutableList<ChipSpec>>()
    var width = 0
    chips.forEach { c ->
        val w = c.text.length * 14 + 28 // 대략의 폭(dp)
        if (rows.isEmpty() || width + w > 340) { rows += mutableListOf(c); width = w } else { rows.last() += c; width += w + 6 }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(Modifier.wrapContentHeight(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { c -> Chip(c.text, c.selected, c.danger, c.enabled, c.onClick) }
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

@Composable
private fun Chip(text: String, selected: Boolean, danger: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    val bg = when {
        selected -> Color.White
        danger -> Danger.copy(alpha = 0.25f)
        else -> Color(0xFF2A2E36)
    }
    val fg = when {
        selected -> Color.Black
        !enabled -> Dim
        danger -> Danger
        else -> Color.White
    }
    Text(
        text,
        color = fg,
        fontSize = 13.sp,
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
