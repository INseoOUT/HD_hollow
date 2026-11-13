package com.hdhollow.studio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/** 색상환 계산(순수). 각도는 색상, 중심에서의 거리는 채도다. 가운데가 흰색, 가장자리가 가장 진한 색. */
object WheelMath {
    /** 터치 위치([x],[y], 중심 기준) → [hue 0..360, sat 0..1]. 원 밖은 가장자리로 붙인다. */
    fun hueSat(x: Float, y: Float, radius: Float): Pair<Float, Float> {
        val hue = ((atan2(y, x) * 180f / PI.toFloat()) + 360f) % 360f
        return hue to (hypot(x, y) / radius).coerceIn(0f, 1f)
    }

    /** [hue],[sat] 의 손잡이 위치(중심 기준). */
    fun position(hue: Float, sat: Float, radius: Float): Pair<Float, Float> {
        val rad = hue * PI.toFloat() / 180f
        return cos(rad) * sat * radius to sin(rad) * sat * radius
    }
}

private val HUES: List<Color> = (0 until 360).map { Color(ColorTemp.hsvToArgb(it.toFloat(), 1f)) }

/**
 * 원형 색상환. 누르거나 끌면 그 자리의 색(가장 밝은 값)을 [onPick] 으로 준다. 밝기는 밝기 슬라이더가 맡는다.
 * [selected] 가 있으면 그 색의 색상·채도 자리에 손잡이를 그린다.
 */
@Composable
fun ColorWheel(selected: Int?, onPick: (Int) -> Unit, modifier: Modifier = Modifier, size: Dp = 168.dp) {
    val pick by rememberUpdatedState(onPick)
    val ring = StudioColors.SwatchRing
    Canvas(
        modifier.size(size)
            .semantics { contentDescription = "무드 색 색상환" }
            .pointerInput(Unit) {
                fun at(p: Offset, s: IntSize) {
                    val r = min(s.width, s.height) / 2f
                    val (h, sat) = WheelMath.hueSat(p.x - s.width / 2f, p.y - s.height / 2f, r)
                    pick(ColorTemp.hsvToArgb(h, sat))
                }
                // 누른 순간부터 가져가서 패널의 세로 스크롤이 끌기를 뺏지 않게 한다
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    at(down.position, this.size)
                    drag(down.id) { it.consume(); at(it.position, this.size) }
                }
            }
    ) {
        val r = this.size.minDimension / 2f
        // 1도씩 부채꼴로 칠한다. sweepGradient 는 0°·180° 에 이음선이 보인다(폰 에뮬레이터에서 확인)
        val box = Size(r * 2, r * 2)
        val topLeft = Offset(center.x - r, center.y - r)
        HUES.forEachIndexed { deg, c -> drawArc(c, deg - 0.5f, 2f, useCenter = true, topLeft = topLeft, size = box) }
        drawCircle(Brush.radialGradient(listOf(Color.White, Color.Transparent), center, r), r, center)
        drawCircle(ring, r, center, style = Stroke(1.dp.toPx()))
        if (selected != null) {
            val hsv = ColorTemp.argbToHsv(selected)
            val (dx, dy) = WheelMath.position(hsv[0], hsv[1], r)
            val c = Offset(center.x + dx, center.y + dy)
            drawCircle(Color.White, 11.dp.toPx(), c)
            drawCircle(Color(ColorTemp.hsvToArgb(hsv[0], hsv[1])), 8.dp.toPx(), c)
            drawCircle(Color.Black.copy(alpha = 0.35f), 11.dp.toPx(), c, style = Stroke(1.dp.toPx()))
        }
    }
}

/** 헥사 입력("#" + 여섯 자리). 여섯 자리가 차면 바로 [onHex] 로 "#RRGGBB" 를 준다. [hex] 는 지금 무드 색(없으면 null). */
@Composable
fun HexField(hex: String?, onHex: (String) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(hex) { mutableStateOf(hex?.removePrefix("#").orEmpty()) }
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier.background(StudioColors.SurfaceHigh, shape).border(1.dp, StudioColors.Line, shape).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("#", color = StudioColors.TextDim, style = MaterialTheme.typography.titleMedium)
        BasicTextField(
            value = text,
            onValueChange = { v ->
                text = v.removePrefix("#").filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' }.take(6).uppercase()
                if (text.length == 6) onHex("#$text")
            },
            modifier = Modifier.weight(1f).semantics { contentDescription = "무드 색 헥사 값" },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = StudioColors.Text),
            cursorBrush = SolidColor(StudioColors.Text),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrect = false, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            decorationBox = { inner ->
                if (text.isEmpty()) Text("RRGGBB", color = StudioColors.TextFaint, style = MaterialTheme.typography.titleMedium)
                inner()
            },
        )
    }
}

/**
 * 조명 색 고르기: 색상환 + 헥사 입력(무드 색) + 흰 빛 톤 칩 셋(색온도). 차 패널과 폰 화면이 같이 쓴다.
 * 색상환·헥사로 고르면 [onMood] 에 "#RRGGBB", 칩을 누르면 [onKelvin] 에 그 색온도(무드 색은 풀린다).
 */
@Composable
fun LightColorPicker(p: LightingParams, onMood: (String) -> Unit, onKelvin: (Int) -> Unit, modifier: Modifier = Modifier) {
    val argb = p.preset?.let(ColorTemp::moodArgb)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        ColorWheel(argb, { onMood(ColorTemp.toHex(it)) })
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HexField(argb?.let(ColorTemp::toHex), onMood, Modifier.fillMaxWidth())
            LightingParams.WHITE_TONES.forEach { k ->
                val on = p.preset == null && p.kelvin == k
                StudioChip(
                    LightLabels.whiteTone(k), selected = on,
                    leading = { ColorDot(Color(ColorTemp.kelvinToArgb(k)), ring = if (on) StudioColors.Ink.copy(alpha = 0.35f) else StudioColors.SwatchRing) },
                    onClick = { onKelvin(k) },
                )
            }
        }
    }
}
