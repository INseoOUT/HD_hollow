package com.hdhollow.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams

/** 둥근 칩. 선택되면 글자색 바탕에 바탕색 글자(어두운 테마에서는 흰 바탕·검은 글자), 삭제 모드는 [danger], 보조 동작은 [outlined]. [leading] 에 색 점을 넣는다. */
@Composable
fun StudioChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
    outlined: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val bg = when {
        selected -> StudioColors.Text
        danger -> StudioColors.Danger.copy(alpha = 0.18f)
        outlined -> Color.Transparent
        else -> StudioColors.SurfaceHigh
    }
    val fg = when {
        selected -> StudioColors.Ink
        !enabled -> StudioColors.TextFaint
        danger -> StudioColors.Danger
        else -> StudioColors.Text
    }
    Row(
        modifier
            .clip(CircleShape)
            .background(bg)
            .then(if (outlined) Modifier.border(1.dp, StudioColors.Line, CircleShape) else Modifier)
            .selectable(selected, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        leading?.invoke()
        Text(text, color = fg, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/** 작은 색 점. 프리셋 칩 안, 앰비언트 줄 앞에 쓴다. [ring] 은 바탕과 구분되는 테두리. */
@Composable
fun ColorDot(color: Color, size: Dp = 14.dp, modifier: Modifier = Modifier, ring: Color = StudioColors.SwatchRing) {
    Box(modifier.size(size).background(color, CircleShape).border(1.dp, ring, CircleShape))
}

/** 둘 이상 중 하나를 고르는 분할 버튼(다이얼 대상 등). */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(modifier.clip(RoundedCornerShape(12.dp)).background(StudioColors.SurfaceHigh).padding(4.dp)) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (on) StudioColors.Text else Color.Transparent)
                    .selectable(on, enabled = enabled, role = Role.RadioButton) { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = if (on) StudioColors.Ink else StudioColors.TextDim, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}

/** 점 + 짧은 글. 연결·송출 상태 등. */
@Composable
fun StatusPill(text: String, dot: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(CircleShape).background(StudioColors.SurfaceHigh).padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(dot, CircleShape))
        Text(text, color = StudioColors.Text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

/** 왼쪽 이름(흐리게), 오른쪽 값(또렷하게). 슬라이더 위에 둔다. */
@Composable
fun LabelValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = StudioColors.TextDim, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = StudioColors.Text, style = MaterialTheme.typography.titleMedium)
    }
}

/** 지금 조명 값 한눈에: 빛나는 색 견본, "7000K · 60%", 느낌 이름. */
@Composable
fun LightReadout(p: LightingParams, modifier: Modifier = Modifier, swatch: Dp = 56.dp) {
    val color = Color(ColorTemp.colorFor(p))
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(swatch)
                .shadow(swatch / 4, CircleShape, ambientColor = color, spotColor = color)
                .background(color, CircleShape)
                .border(2.dp, StudioColors.SwatchEdge, CircleShape)
        )
        Column {
            Text(LightLabels.summary(p), color = StudioColors.Text, style = MaterialTheme.typography.headlineSmall, maxLines = 1)
            Text(LightLabels.tone(p), color = StudioColors.TextDim, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
    }
}

/** 카드. 폰 화면의 묶음 단위. */
@Composable
fun StudioCard(modifier: Modifier = Modifier, padding: Dp = 20.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(StudioColors.Surface).padding(padding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

/** 번호 + 제목 + 설명 한 줄. 사용 순서 안내. */
@Composable
fun StepRow(n: Int, title: String, body: String? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(28.dp).background(StudioColors.SurfaceHigh, CircleShape), contentAlignment = Alignment.Center) {
            Text("$n", color = StudioColors.Text, style = MaterialTheme.typography.labelMedium)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = StudioColors.Text, style = MaterialTheme.typography.bodyLarge)
            if (body != null) Text(body, color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall)
        }
    }
}
