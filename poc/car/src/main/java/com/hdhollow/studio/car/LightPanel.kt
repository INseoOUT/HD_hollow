package com.hdhollow.studio.car

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdhollow.studio.shared.Protocol
import com.hdhollow.studio.ui.CarScreenSection
import com.hdhollow.studio.ui.ColorDot
import com.hdhollow.studio.ui.ColorSection
import com.hdhollow.studio.ui.PresetSection
import com.hdhollow.studio.ui.RingLightSection
import com.hdhollow.studio.ui.StatusPill
import com.hdhollow.studio.ui.StudioColors

/*
 * 조명 패널. 내용은 두 묶음이다.
 *  - 조명([LightControls]): 이름·폰 연결 상태, 링라이트(지금 값·밝기), 프리셋(기본 4 + 사용자, 추가·삭제)
 *  - 화면([ScreenControls]): 색(색상환·흰 빛 톤), 차 화면(다이얼 대상, 앰비언트 연동·현재 값, 테마), 맨 아래 풀 라이트 버튼
 * 세로 영상이면 모니터 양옆에 한 묶음씩 두고(스크롤 없이 다 보인다), 가로 영상이면 오른쪽 한 칸에 이어 붙인다([PanelLayout]).
 * 구역은 폰 화면과 같은 컴포넌트(poc/ui StudioSections)다. 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바뀐다(2026-11-09 결정).
 */

/**
 * 패널 한 칸. [content] 는 넘치면 스크롤되고 아래로 더 있으면 끝을 흐리게 해 알린다. [footer] 는 스크롤과 상관없이 맨 아래에 붙어 있다.
 */
@Composable
fun StudioPanel(modifier: Modifier = Modifier, footer: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    val scrim = StudioColors.PanelScrim.copy(alpha = 1f)
    Column(
        modifier
            .fillMaxHeight()
            .width(PanelLayout.PANEL_DP.dp)
            .background(StudioColors.PanelScrim, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(
            Modifier.weight(1f).drawWithContent {
                drawContent()
                if (scroll.canScrollForward) {
                    val fade = 56.dp.toPx()
                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, scrim), startY = size.height - fade, endY = size.height))
                }
            }
        ) {
            Column(Modifier.verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(18.dp), content = content)
        }
        footer?.invoke()
    }
}

/** 구역 사이의 선. */
@Composable
fun PanelDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(StudioColors.Line))
}

/** 조명 묶음: 이름·폰 연결 상태 → 링라이트(지금 값·밝기) → 프리셋. */
@Composable
fun LightControls(light: StudioLight, lighting: Lighting, studio: StudioVehicle, presets: ScenePresets, connected: Boolean, streaming: Boolean) {
    val userScenes by presets.user.collectAsState()
    val params = light.params(Protocol.ROLE_CAR)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("SELENE", color = StudioColors.TextDim, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
        StatusPill(
            text = when { streaming -> "송출 중"; connected -> "폰 연결됨"; else -> "폰 대기 중" },
            dot = if (connected || streaming) StudioColors.Ok else StudioColors.TextFaint,
        )
    }
    PanelDivider()
    RingLightSection(params, onLevel = { light.level = it })
    PanelDivider()
    PresetSection(
        params, userScenes,
        onApply = { scene ->
            CarScenes.applyLight(scene, light)
            CarScenes.applyVehicle(scene, lighting, studio)
        },
        onSave = { presets.saveCurrent(light) },
        onDelete = { presets.delete(it.id) },
    )
}

/** 화면 묶음: 색(색상환·흰 빛 톤) → 차 화면(다이얼 대상, 앰비언트 연동·현재 값, 테마). 풀 라이트는 [FullLightButton] 으로 따로 둔다. */
@Composable
fun ScreenControls(light: StudioLight, lighting: Lighting, presets: ScenePresets) {
    val userScenes by presets.user.collectAsState()
    val params = light.params(Protocol.ROLE_CAR)

    // 헥사 입력은 차에 두지 않는다(화면 키보드로 여섯 자리를 칠 일이 드물다). 폰에서 넣으면 여기 색상환에 반영된다
    ColorSection(params, onKelvin = light::chooseKelvin, onMood = { light.choosePreset(it) }, showHex = false)
    PanelDivider()
    // 에뮬레이터에선 다이얼 누름·nudge 가 앱에 안 와서(RotaryService 가 소비) 터치로 대상을 바꾼다
    CarScreenSection(
        light.state(userScenes), onChange = light::apply,
        showAmbient = lighting.ambient.isNotEmpty(), showFullLight = false,
    ) {
        // 차량에서 읽은 앰비언트의 지금 값. 연동이 실제로 들어갔는지 색 점과 밝기로 보여 준다.
        // 스위치 값은 보이지 않는다: 에뮬레이터 2.1.0 에서 앰비언트가 켜져 있어도(Virtual Console mode=On) SDK 는 OFF 로 읽는다
        lighting.ambient.forEach { (area, a) ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ColorDot(a.color?.let { Color(0xFF000000.toInt() or it) } ?: StudioColors.Line)
                Text(AmbientLabel.of(area.label(), a.brightness), color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** 차량 앰비언트 한 영역의 지금 값을 사람 말로. 테스트 대상. */
object AmbientLabel {
    /** "전체 · 밝기 70". 밝기를 아직 못 읽었으면 "–". */
    fun of(area: String, brightness: Int?): String = "$area · 밝기 ${brightness ?: "–"}"
}

/**
 * 풀 라이트: 모니터·패널을 숨기고 차 화면 전체를 조명으로. 패널 맨 아래에 늘 보이는 버튼이다
 * (스위치로 두면 켠 순간 패널이 사라져 '켜짐'을 볼 일이 없다). 높이는 Android Automotive 가이드의 76dp.
 */
@Composable
fun FullLightButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.fillMaxWidth().height(76.dp)
            .clip(shape)
            .background(StudioColors.SurfaceHigh)
            .border(1.dp, StudioColors.Line, shape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("풀 라이트", color = StudioColors.Text, style = MaterialTheme.typography.labelLarge)
        Text("화면 전체를 조명으로", color = StudioColors.TextDim, style = MaterialTheme.typography.labelSmall)
    }
}
