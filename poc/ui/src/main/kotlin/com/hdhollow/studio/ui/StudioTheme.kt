package com.hdhollow.studio.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 스튜디오 팔레트. 어두운 방에서 빛(링라이트·앰비언트·무드 색)이 주인공이라 UI 는 무채색으로 물러난다.
 * 채도 있는 색은 상태 표시(연결·경고·삭제)에만 쓴다. 차와 폰이 같은 값을 쓴다.
 */
object StudioColors {
    val Ink = Color(0xFF0B0C0F) // 가장 어두운 바닥(모니터 뒤, 폰 화면 배경)
    val Surface = Color(0xFF14161B) // 카드
    val SurfaceHigh = Color(0xFF1E2129) // 칩·분할 버튼·입력 바탕
    val Line = Color(0xFF2B2F38)
    val Text = Color(0xFFF4F5F7)
    val TextDim = Color(0xFF9AA0A8)
    val TextFaint = Color(0xFF666B74)
    val Ok = Color(0xFF5BE49B)
    val Warn = Color(0xFFFFC56B)
    val Danger = Color(0xFFFF8A7A)
    val PanelScrim = Color(0xF0121419) // 링라이트 위에 얹는 반투명 패널
}

private val Scheme = darkColorScheme(
    primary = StudioColors.Text,
    onPrimary = StudioColors.Ink,
    primaryContainer = StudioColors.SurfaceHigh,
    onPrimaryContainer = StudioColors.Text,
    secondary = StudioColors.TextDim,
    onSecondary = StudioColors.Ink,
    secondaryContainer = StudioColors.SurfaceHigh,
    onSecondaryContainer = StudioColors.Text,
    background = StudioColors.Ink,
    onBackground = StudioColors.Text,
    surface = StudioColors.Ink,
    onSurface = StudioColors.Text,
    surfaceVariant = StudioColors.Surface,
    onSurfaceVariant = StudioColors.TextDim,
    surfaceContainer = StudioColors.Surface,
    surfaceContainerHigh = StudioColors.SurfaceHigh,
    outline = StudioColors.Line,
    outlineVariant = StudioColors.Line,
    error = StudioColors.Danger,
    onError = StudioColors.Ink,
)

private fun type(size: Int, weight: FontWeight = FontWeight.Normal): TextStyle =
    TextStyle(fontSize = size.sp, lineHeight = (size * 1.35f).sp, fontWeight = weight, letterSpacing = 0.sp)

/** 폰 글자 크기. M3 기본과 비슷하다. */
private val PhoneType = Typography(
    headlineMedium = type(28, FontWeight.SemiBold),
    headlineSmall = type(24, FontWeight.SemiBold),
    titleLarge = type(22, FontWeight.SemiBold),
    titleMedium = type(17, FontWeight.SemiBold),
    titleSmall = type(14, FontWeight.Medium),
    bodyLarge = type(16),
    bodyMedium = type(14),
    bodySmall = type(13),
    labelLarge = type(15, FontWeight.Medium),
    labelMedium = type(13, FontWeight.Medium),
    labelSmall = type(11, FontWeight.Medium),
)

/** 차 글자 크기. 70cm 거리·160dpi 에서 읽히도록 폰보다 한 단계 크고, 보조 글자도 15sp 아래로 내리지 않는다. */
private val CarType = Typography(
    headlineMedium = type(40, FontWeight.SemiBold),
    headlineSmall = type(32, FontWeight.SemiBold),
    titleLarge = type(26, FontWeight.SemiBold),
    titleMedium = type(20, FontWeight.SemiBold),
    titleSmall = type(17, FontWeight.Medium),
    bodyLarge = type(19),
    bodyMedium = type(17),
    bodySmall = type(15),
    labelLarge = type(18, FontWeight.Medium),
    labelMedium = type(16, FontWeight.Medium),
    labelSmall = type(14, FontWeight.Medium),
)

/** 두 앱의 공통 테마. [car] 면 차 글자 크기. 항상 어두운 테마다(스튜디오 조명 앱이라 화면 자체가 빛을 내면 안 된다). */
@Composable
fun StudioTheme(car: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = if (car) CarType else PhoneType, content = content)
}
