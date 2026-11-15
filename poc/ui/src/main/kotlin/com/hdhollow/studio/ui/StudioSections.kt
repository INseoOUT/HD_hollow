package com.hdhollow.studio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.Scene
import com.hdhollow.studio.shared.Scenes
import com.hdhollow.studio.shared.StudioState

/*
 * 차 패널과 폰 화면이 똑같이 쓰는 구역 네 개: 링라이트(지금 값·밝기), 프리셋, 색, 차 화면.
 * 순서는 자주 쓰는 것부터다: 지금 값을 보고 → 프리셋 한 번 탭 → 밝기 → 색을 직접 고르기 → 차 화면 설정.
 * 내용·순서·문구가 한 곳에 있어 두 앱의 구성이 어긋나지 않는다. 묶는 모양만 다르다(차는 패널 안의 구역, 폰은 카드).
 */

/**
 * 구역 하나: 흐린 제목 + 내용. 폰에서는 카드에 넣고, 차에서는 패널이 구역 사이에 선을 긋는다.
 * 제목 오른쪽에 [value](지금 값, 또렷하게)나 [note](작은 글)를 둔다.
 */
@Composable
fun StudioSection(
    title: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    value: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val head: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = StudioColors.TextDim, style = MaterialTheme.typography.titleSmall)
            when {
                value != null -> Text(value, color = StudioColors.Text, style = MaterialTheme.typography.titleMedium)
                note != null -> Text(note, color = StudioColors.TextFaint, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    if (LocalStudioCar.current) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            head()
            content()
        }
    } else {
        StudioCard(modifier) {
            head()
            content()
        }
    }
}

/** 링라이트: 지금 값 읽기와 밝기. 가장 자주 보는 것과 가장 자주 만지는 것을 맨 위에 둔다. */
@Composable
fun RingLightSection(p: LightingParams, onLevel: (Float) -> Unit, modifier: Modifier = Modifier, note: String? = null) {
    StudioSection("링라이트", modifier, note) {
        LightReadout(p)
        LabelValue("밝기", LightLabels.percent(p.level))
        LevelSlider(p.level, Color(ColorTemp.colorFor(p)), onLevel, Modifier.fillMaxWidth())
    }
}

/**
 * 색: 색상환·헥사 입력(무드 색)과 흰 빛 톤 칩(색온도). 프리셋으로 안 되는 색을 직접 고를 때 쓴다.
 * 색은 무드 색과 흰 빛 중 하나다. 색상환이나 헥사로 고르면 무드 색이 되고([onMood] 에 "#RRGGBB"),
 * 흰 빛 톤 칩을 누르면 그 색온도의 흰 빛으로 돌아간다([onKelvin]). [showHex] 는 [LightColorPicker] 참고.
 */
@Composable
fun ColorSection(
    p: LightingParams,
    onKelvin: (Int) -> Unit,
    onMood: (String) -> Unit,
    modifier: Modifier = Modifier,
    showHex: Boolean = true,
) {
    StudioSection("색", modifier, value = LightLabels.color(p)) {
        LightColorPicker(p, onMood = onMood, onKelvin = onKelvin, modifier = Modifier.fillMaxWidth(), showHex = showHex)
    }
}

/**
 * 프리셋: 기본 4개 + 사용자 프리셋 칩(색 점), 지금 값 저장, 삭제 모드.
 * 저장·삭제는 차가 하므로 [canEdit] 가 false(폰이 차와 떨어져 있음)면 고르기만 된다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetSection(
    p: LightingParams,
    userScenes: List<Scene>,
    onApply: (Scene) -> Unit,
    onSave: () -> Unit,
    onDelete: (Scene) -> Unit,
    modifier: Modifier = Modifier,
    canEdit: Boolean = true,
) {
    var editMode by remember { mutableStateOf(false) } // 삭제 모드: 사용자 프리셋을 탭하면 지운다
    val editing = editMode && canEdit && userScenes.isNotEmpty()
    val all = Scenes.BUILT_IN + userScenes
    val current = Scenes.matching(p, all)
    StudioSection("프리셋", modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            all.forEach { scene ->
                val deletable = editing && !scene.builtIn
                val selected = !editing && current?.id == scene.id
                StudioChip(
                    text = if (deletable) "× ${scene.name}" else scene.name,
                    selected = selected,
                    enabled = !editing || !scene.builtIn,
                    danger = deletable,
                    // 선택된 칩은 바탕이 글자색으로 뒤집히므로 색 점 테두리도 반대쪽 색으로
                    leading = { ColorDot(Color(Scenes.argb(scene)), ring = if (selected) StudioColors.Ink.copy(alpha = 0.35f) else StudioColors.SwatchRing) },
                    onClick = { if (editing) { if (!scene.builtIn) onDelete(scene) } else onApply(scene) },
                )
            }
        }
        if (canEdit) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudioChip("+ 지금 값 저장", selected = false, outlined = true, onClick = { onSave(); editMode = false })
                if (userScenes.isNotEmpty()) {
                    StudioChip(if (editing) "완료" else "삭제", selected = editing, outlined = !editing, onClick = { editMode = !editing })
                }
            }
            if (editing) Text("지울 프리셋을 탭하세요. 기본 프리셋은 지울 수 없습니다.", color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall)
        } else {
            Text("차와 연결되면 지금 값을 프리셋으로 저장하거나 지울 수 있습니다.", color = StudioColors.TextFaint, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * 차 화면: 다이얼 대상, 앰비언트 연동, 풀 라이트, 차 화면 테마. 모두 스튜디오 앱의 상태([StudioState])다.
 * [enabled] 가 false(폰이 차와 떨어져 있음)면 흐리게 두고 누를 수 없다. [extra] 는 그 화면에만 있는 덧붙임(차의 앰비언트 구역 값).
 * [showFullLight] 가 false 면 풀 라이트 줄을 뺀다(차: 패널 맨 아래에 늘 보이는 버튼으로 따로 둔다).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarScreenSection(
    state: StudioState,
    onChange: (StudioState) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    note: String? = null,
    showAmbient: Boolean = true,
    showFullLight: Boolean = true,
    extra: @Composable ColumnScope.() -> Unit = {},
) {
    StudioSection("차 화면", modifier, note) {
        Column(Modifier.alpha(if (enabled) 1f else 0.45f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("다이얼", color = StudioColors.TextDim, style = MaterialTheme.typography.bodyMedium)
                Segmented(listOf("밝기", "흰 빛 톤"), if (state.dialColor) 1 else 0, { onChange(state.copy(dialColor = it == 1)) }, Modifier.fillMaxWidth(), enabled)
                Text(LightLabels.dialStep(state.dialColor), color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall)
            }
            if (showAmbient) {
                ToggleRow("앰비언트 연동", "링라이트 색과 밝기를 실내 앰비언트에도", state.ambientLinked, { onChange(state.copy(ambientLinked = it)) }, enabled)
                extra()
            }
            // 폰 화면을 직접 봐야 할 때(화상회의): 모니터·패널을 숨기고 전체를 조명으로. 차 화면을 탭해도 돌아온다
            if (showFullLight) {
                ToggleRow("풀 라이트", "모니터·패널을 숨기고 차 화면 전체를 조명으로", state.fullLight, { onChange(state.copy(fullLight = it)) }, enabled)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("테마", color = StudioColors.TextDim, style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CarThemes.ids.forEach { id ->
                        val on = state.theme == id
                        StudioChip(
                            CarThemes.label(id), selected = on, enabled = enabled,
                            leading = { ColorDot(CarThemes.palette(id).chip, ring = if (on) StudioColors.Ink.copy(alpha = 0.35f) else StudioColors.SwatchRing) },
                            onClick = { onChange(state.copy(theme = id)) },
                        )
                    }
                }
            }
        }
    }
}

/** 제목 + 설명 + 스위치 한 줄. 스위치만이 아니라 줄 전체가 눌린다(스위치는 52×32dp 라 차에서 누르기 작다). */
@Composable
fun ToggleRow(title: String, body: String?, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth()
            .heightIn(min = StudioSizes.Touch)
            .clip(RoundedCornerShape(12.dp))
            .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = StudioColors.Text, style = MaterialTheme.typography.bodyLarge)
            if (body != null) Text(body, color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked, onCheckedChange = null, enabled = enabled)
    }
}
