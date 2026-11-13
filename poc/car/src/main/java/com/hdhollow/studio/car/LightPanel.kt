package com.hdhollow.studio.car

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdhollow.studio.shared.Protocol
import com.hdhollow.studio.ui.CarScreenSection
import com.hdhollow.studio.ui.ColorDot
import com.hdhollow.studio.ui.PresetSection
import com.hdhollow.studio.ui.RingLightSection
import com.hdhollow.studio.ui.StatusPill
import com.hdhollow.studio.ui.StudioColors

/** 오른쪽 조명 패널 폭. 앱 영역(약 1680px, 160dpi)에서 세로 폰 화면 옆에 들어간다. 글자를 키우며 380 → 420. */
val PANEL_WIDTH = 420.dp

/**
 * 조명 패널. 위에서부터: 이름·폰 연결 상태, 링라이트(지금 값·밝기·색: 색상환·헥사·흰 빛 톤), 프리셋(기본 4 + 사용자, 추가·삭제),
 * 차 화면(다이얼 대상, 앰비언트 연동·현재 값, 풀 라이트, 테마). 구역은 폰 화면과 같은 컴포넌트(poc/ui StudioSections)다.
 * 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바뀐다(2026-11-09 결정).
 */
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
    val params = light.params(Protocol.ROLE_CAR)

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
            Text("SELENE", color = StudioColors.TextDim, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            StatusPill(
                text = when { streaming -> "송출 중"; connected -> "폰 연결됨"; else -> "폰 대기 중" },
                dot = if (connected || streaming) StudioColors.Ok else StudioColors.TextFaint,
            )
        }

        RingLightSection(params, onLevel = { light.level = it }, onKelvin = light::chooseKelvin, onMood = light::choosePreset)

        PresetSection(
            params, userScenes,
            onApply = { scene ->
                CarScenes.applyLight(scene, light)
                CarScenes.applyVehicle(scene, lighting, studio)
            },
            onSave = { presets.saveCurrent(light) },
            onDelete = { presets.delete(it.id) },
        )

        // 에뮬레이터에선 다이얼 누름·nudge 가 앱에 안 와서(RotaryService 가 소비) 터치로 대상을 바꾼다
        CarScreenSection(light.state(userScenes), onChange = light::apply, showAmbient = lighting.ambient.isNotEmpty()) {
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
}
