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

/**
 * 트랙이 그라데이션인 슬라이더. 트랙은 "이쪽으로 가면 어떤 빛이 되는지"를, 손잡이 속은 "지금 빛"을 보여 준다.
 * 제스처·단계·접근성은 M3 [Slider] 그대로고 그림만 바꾼다. 손잡이·트랙·누르는 높이는 [StudioSizes](차가 더 크다).
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
    val thumbSize = StudioSizes.Thumb
    val trackHeight = StudioSizes.Track
    val shape = RoundedCornerShape(trackHeight / 2)
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(StudioSizes.Touch),
        valueRange = valueRange,
        steps = steps,
        interactionSource = interaction,
        thumb = {
            Box(
                Modifier.size(thumbSize)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(1.dp, StudioColors.SwatchRing, CircleShape)
                    .padding(thumbSize / 7)
                    .background(thumbColor, CircleShape)
            )
        },
        track = {
            Box(
                Modifier.fillMaxWidth().height(trackHeight)
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
