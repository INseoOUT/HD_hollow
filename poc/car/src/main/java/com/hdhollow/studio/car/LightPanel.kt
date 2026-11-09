package com.hdhollow.studio.car

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.Protocol
import com.hdhollow.studio.ui.ColorDot
import com.hdhollow.studio.ui.KelvinSlider
import com.hdhollow.studio.ui.LabelValue
import com.hdhollow.studio.ui.LevelSlider
import com.hdhollow.studio.ui.LightLabels
import com.hdhollow.studio.ui.LightReadout
import com.hdhollow.studio.ui.MoodDots
import com.hdhollow.studio.ui.Segmented
import com.hdhollow.studio.ui.StatusPill
import com.hdhollow.studio.ui.StudioChip
import com.hdhollow.studio.ui.StudioColors

/** 오른쪽 조명 패널 폭. 앱 영역(약 1680px, 160dpi)에서 세로 폰 화면 옆에 들어간다. 글자를 키우며 380 → 420. */
val PANEL_WIDTH = 420.dp

/**
 * 조명 패널. 위에서부터: 이름·폰 연결 상태, 지금 값 읽기(색 견본·"7000K · 60%"·느낌), 프리셋(기본 4 + 사용자, 추가·삭제),
 * 링라이트(밝기·색온도 그라데이션 슬라이더, 무드 색), 다이얼 대상, 앰비언트(연동·현재 값), 풀 라이트.
 * 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바뀐다(2026-11-09 결정).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LightPanel(
    light: StudioLight,
    lighting: Lighting,
    studio: StudioVehicle,
    presets: ScenePresets,
    connected: Boolean,
    streaming: Boolean,
    modifier: Modifier = Modifier,
) {
    val userScenes by presets.user.collectAsState()
    var editing by remember { mutableStateOf(false) } // 삭제 모드: 사용자 프리셋을 탭하면 지운다
    val params = light.params(Protocol.ROLE_CAR)
    val tint = Color(light.argb)

    Column(
        modifier
            .fillMaxHeight()
            .width(PANEL_WIDTH)
            .background(StudioColors.PanelScrim, RoundedCornerShape(24.dp))
            .padding(horizontal = 22.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("CAR STUDIO", color = StudioColors.TextDim, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            StatusPill(
                text = when { streaming -> "송출 중"; connected -> "폰 연결됨"; else -> "폰 대기 중" },
                dot = if (connected || streaming) StudioColors.Ok else StudioColors.TextFaint,
            )
        }
        LightReadout(params)

        Section("프리셋") {
            val all = Scenes.BUILT_IN + userScenes
            val current = Scenes.matching(light, all)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                all.forEach { scene ->
                    val deletable = editing && !scene.builtIn
                    val selected = !editing && current?.id == scene.id
                    StudioChip(
                        text = if (deletable) "× ${scene.name}" else scene.name,
                        selected = selected,
                        enabled = !editing || !scene.builtIn,
                        danger = deletable,
                        // 흰 칩 위에서는 7000K 같은 밝은 색 점이 묻히므로 어두운 링
                        leading = { ColorDot(Color(Scenes.argb(scene)), ring = if (selected) StudioColors.Ink.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.25f)) },
                        onClick = {
                            if (editing) {
                                if (!scene.builtIn) presets.delete(scene.id)
                            } else {
                                Scenes.applyLight(scene, light)
                                Scenes.applyVehicle(scene, lighting, studio)
                            }
                        },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioChip("+ 지금 값 저장", selected = false, outlined = true, onClick = { presets.saveCurrent(light); editing = false })
                if (userScenes.isNotEmpty()) {
                    StudioChip(if (editing) "완료" else "삭제", selected = editing, outlined = !editing, onClick = { editing = !editing })
                }
            }
            if (editing) Text("지울 프리셋을 탭하세요. 기본 프리셋은 지울 수 없습니다.", color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall)
        }

        Section("링라이트") {
            LabelValue("밝기", LightLabels.percent(light.level))
            LevelSlider(light.level, tint, { light.level = it }, Modifier.fillMaxWidth())
            LabelValue("색온도", LightLabels.color(params))
            KelvinSlider(light.kelvin, light::chooseKelvin, Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("무드 색", color = StudioColors.TextDim, style = MaterialTheme.typography.bodyMedium)
                MoodDots(light.preset, light::choosePreset)
            }
        }

        Section("다이얼") {
            // 에뮬레이터에선 다이얼 누름·nudge 가 앱에 안 와서(RotaryService 가 소비) 터치로 대상을 바꾼다
            Segmented(listOf("밝기", "색온도"), if (light.dialColor) 1 else 0, { light.dialColor = it == 1 }, Modifier.fillMaxWidth())
            Text(
                "다이얼 1칸 = ${if (light.dialColor) "${LightingParams.KELVIN_DIAL_STEP}K" else "${(LightingParams.LEVEL_DIAL_STEP * 100).toInt()}%"}",
                color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall,
            )
        }

        if (lighting.ambient.isNotEmpty()) {
            Section("앰비언트") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("링라이트와 연동", color = StudioColors.Text, style = MaterialTheme.typography.bodyLarge)
                    Switch(light.ambientLinked, { light.ambientLinked = it })
                }
                lighting.ambient.forEach { (area, a) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        ColorDot(a.color?.let { Color(0xFF000000.toInt() or it) } ?: StudioColors.Line, 12.dp)
                        Text(
                            "${area.label()} · 밝기 ${a.brightness ?: "?"}" + (a.switch?.let { " · ${it.name.lowercase()}" } ?: ""),
                            color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        // 폰 화면을 직접 봐야 할 때(화상회의): 모니터·패널을 숨기고 전체를 조명으로. 화면 탭으로 복귀
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, StudioColors.Line, RoundedCornerShape(16.dp))
                .clickable { light.fullLight = true }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("풀 라이트", color = StudioColors.Text, style = MaterialTheme.typography.titleMedium)
                Text("모니터·패널을 숨기고 화면 전체를 조명으로", color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall)
            }
            Text("›", color = StudioColors.TextDim, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

/** 구역: 위 선 + 흐린 제목 + 내용. */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(StudioColors.Line))
        Text(title, color = StudioColors.TextDim, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
        content()
    }
}
