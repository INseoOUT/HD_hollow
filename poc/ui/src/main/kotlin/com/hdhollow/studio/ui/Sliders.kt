package com.hdhollow.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import kotlin.math.roundToInt

private val THUMB = 28.dp
private val TRACK = 14.dp

/**
 * 트랙이 그라데이션인 슬라이더. 트랙은 "이쪽으로 가면 어떤 빛이 되는지"를, 손잡이 속은 "지금 빛"을 보여 준다.
 * 제스처·단계·접근성은 M3 [Slider] 그대로고 그림만 바꾼다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    colors: List<Color>,
    thumbColor: Color,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(TRACK / 2)
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(40.dp),
        valueRange = valueRange,
        steps = steps,
        interactionSource = interaction,
        thumb = {
            Box(
                Modifier.size(THUMB)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(1.dp, StudioColors.SwatchRing, CircleShape)
                    .padding(4.dp)
                    .background(thumbColor, CircleShape)
            )
        },
        track = {
            Box(
                Modifier.fillMaxWidth().height(TRACK)
                    .clip(shape)
                    .background(Brush.horizontalGradient(colors))
                    .border(1.dp, StudioColors.SwatchRing, shape)
            )
        },
    )
}

/** 밝기. 트랙은 어둠 → 지금 색, 손잡이 속은 지금 밝기의 그 색. */
@Composable
fun LevelSlider(level: Float, tint: Color, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    GradientSlider(
        value = level,
        onValueChange = onChange,
        colors = listOf(StudioPalettes.Black.ink, tint), // 테마와 상관없이 '꺼짐'은 검정
        thumbColor = lerp(Color.Black, tint, level),
        modifier = modifier,
    )
}

/** 색온도. 트랙은 2700K(전구색) → 8000K(푸른빛)의 실제 색(ColorTemp). 단위는 슬라이더 단계(100K). */
@Composable
fun KelvinSlider(kelvin: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    GradientSlider(
        value = kelvin.toFloat(),
        onValueChange = { v -> onChange((v / LightingParams.KELVIN_SLIDER_STEP).roundToInt() * LightingParams.KELVIN_SLIDER_STEP) },
        colors = KELVIN_STOPS,
        thumbColor = Color(ColorTemp.kelvinToArgb(kelvin)),
        modifier = modifier,
        valueRange = LightingParams.KELVIN_MIN.toFloat()..LightingParams.KELVIN_MAX.toFloat(),
        steps = (LightingParams.KELVIN_MAX - LightingParams.KELVIN_MIN) / LightingParams.KELVIN_SLIDER_STEP - 1,
    )
}

private val KELVIN_STOPS: List<Color> =
    (LightingParams.KELVIN_MIN..LightingParams.KELVIN_MAX step LightingParams.KELVIN_SLIDER_STEP).map { Color(ColorTemp.kelvinToArgb(it)) }
