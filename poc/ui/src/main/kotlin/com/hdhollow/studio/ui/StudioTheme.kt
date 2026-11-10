package com.hdhollow.studio.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hdhollow.studio.shared.StudioState

/**
 * 색 토큰 한 벌. 어두운 방에서 빛(링라이트·앰비언트·무드 색)이 주인공이라 UI 는 채도를 거의 쓰지 않고 물러난다.
 * 채도 있는 색은 상태 표시(연결·경고·삭제)에만 쓴다. 차와 폰이 같은 컴포넌트에 서로 다른 벌을 넣는다:
 * 차는 실내 마감에 맞춘 어두운 세트([CarThemes]), 폰은 시스템 설정을 따르는 라이트·다크.
 */
data class StudioPalette(
    val ink: Color, // 가장 깊은 바닥(모니터 뒤, 폰 화면 배경)
    val surface: Color, // 카드
    val surfaceHigh: Color, // 칩·분할 버튼·입력 바탕
    val line: Color,
    val text: Color,
    val textDim: Color,
    val textFaint: Color,
    val ok: Color,
    val warn: Color,
    val danger: Color,
    val panelScrim: Color, // 링라이트 위에 얹는 반투명 패널
    val swatchRing: Color, // 색 점·슬라이더 트랙의 가는 테두리(바탕과 구분)
    val swatchEdge: Color, // 큰 색 견본의 테두리
    val chip: Color, // 테마 고르기 칩의 대표 색 점
    val light: Boolean = false,
)

object StudioPalettes {
    /** 블랙: 중성 무채색. 검은 실내, 폰의 다크 모드. */
    val Black = StudioPalette(
        ink = Color(0xFF0B0B0C), surface = Color(0xFF151516), surfaceHigh = Color(0xFF202022), line = Color(0xFF2E2E31),
        text = Color(0xFFF4F5F7), textDim = Color(0xFF9E9FA3), textFaint = Color(0xFF6A6B6F),
        ok = Color(0xFF5BE49B), warn = Color(0xFFFFC56B), danger = Color(0xFFFF8A7A),
        panelScrim = Color(0xF0131314), swatchRing = Color(0x40FFFFFF), swatchEdge = Color(0x99FFFFFF), chip = Color(0xFF3A3A3D),
    )

    /** 웜 그레이지: 따뜻한 기가 도는 차콜. 베이지·브라운 실내. */
    val Greige = Black.copy(
        ink = Color(0xFF0F0D0B), surface = Color(0xFF1A1714), surfaceHigh = Color(0xFF27221D), line = Color(0xFF383129),
        text = Color(0xFFF7F4EF), textDim = Color(0xFFA8A096), textFaint = Color(0xFF756D63),
        panelScrim = Color(0xF0191613), chip = Color(0xFF8C7A66),
    )

    /** 네이비: 푸른 기가 도는 차콜. 네이비·그레이 실내. */
    val Navy = Black.copy(
        ink = Color(0xFF090C12), surface = Color(0xFF111620), surfaceHigh = Color(0xFF1A2130), line = Color(0xFF283246),
        text = Color(0xFFF2F5FA), textDim = Color(0xFF96A1B3), textFaint = Color(0xFF616C7F),
        panelScrim = Color(0xF00F141D), chip = Color(0xFF3A4E73),
    )

    /** 폰의 라이트 모드. 차에는 쓰지 않는다(차 화면은 그 자체가 조명이라 밝은 바탕이 빛을 흐린다). */
    val Light = StudioPalette(
        ink = Color(0xFFF3F4F6), surface = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFE8EAEE), line = Color(0xFFD3D6DC),
        text = Color(0xFF14161B), textDim = Color(0xFF555B66), textFaint = Color(0xFF6F7580),
        ok = Color(0xFF0B8A4A), warn = Color(0xFF9A5B00), danger = Color(0xFFC8372A),
        panelScrim = Color(0xF0FFFFFF), swatchRing = Color(0x33000000), swatchEdge = Color(0x2E000000), chip = Color(0xFFE8EAEE),
        light = true,
    )
}

val LocalStudioPalette = staticCompositionLocalOf { StudioPalettes.Black }

/** 차 화면인가. 구역([StudioSection])이 차에서는 구분선, 폰에서는 카드로 묶인다. 내용 구성은 같다. */
val LocalStudioCar = staticCompositionLocalOf { false }

/** 지금 테마의 색. 컴포저블 안에서 `StudioColors.Text` 처럼 쓴다. 테마 밖(오버레이 창 등)에서는 [StudioPalettes] 를 직접 쓴다. */
object StudioColors {
    val Ink: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.ink
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.surface
    val SurfaceHigh: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.surfaceHigh
    val Line: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.line
    val Text: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.text
    val TextDim: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.textDim
    val TextFaint: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.textFaint
    val Ok: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.ok
    val Warn: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.warn
    val Danger: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.danger
    val PanelScrim: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.panelScrim
    val SwatchRing: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.swatchRing
    val SwatchEdge: Color @Composable @ReadOnlyComposable get() = LocalStudioPalette.current.swatchEdge
}

/** 차 화면 테마. id 는 계약([StudioState.THEMES])과 같다. 모두 어두운 저채도 세트다. */
object CarThemes {
    val ids: List<String> get() = StudioState.THEMES

    fun palette(id: String): StudioPalette = when (id) {
        StudioState.THEME_GREIGE -> StudioPalettes.Greige
        StudioState.THEME_NAVY -> StudioPalettes.Navy
        else -> StudioPalettes.Black
    }

    fun label(id: String): String = when (id) {
        StudioState.THEME_GREIGE -> "웜 그레이지"
        StudioState.THEME_NAVY -> "네이비"
        else -> "블랙"
    }
}

private fun scheme(p: StudioPalette): ColorScheme {
    val base = if (p.light) lightColorScheme() else darkColorScheme()
    return base.copy(
        primary = p.text,
        onPrimary = p.ink,
        primaryContainer = p.surfaceHigh,
        onPrimaryContainer = p.text,
        secondary = p.textDim,
        onSecondary = p.ink,
        secondaryContainer = p.surfaceHigh,
        onSecondaryContainer = p.text,
        background = p.ink,
        onBackground = p.text,
        surface = p.ink,
        onSurface = p.text,
        surfaceVariant = p.surfaceHigh,
        onSurfaceVariant = p.textDim,
        surfaceContainer = p.surface,
        surfaceContainerHigh = p.surfaceHigh,
        surfaceContainerHighest = p.surfaceHigh,
        outline = p.textFaint,
        outlineVariant = p.line,
        error = p.danger,
        onError = p.ink,
    )
}

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

/**
 * 두 앱의 공통 테마. [car] 면 차 글자 크기. [palette] 를 주지 않으면 시스템 설정을 따라 라이트·다크(폰).
 * 차는 [CarThemes.palette] 로 어두운 세트만 넣는다(차 화면 자체가 조명이라 UI 가 빛을 내면 안 된다).
 */
@Composable
fun StudioTheme(
    car: Boolean = false,
    palette: StudioPalette = if (isSystemInDarkTheme()) StudioPalettes.Black else StudioPalettes.Light,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalStudioPalette provides palette, LocalStudioCar provides car) {
        MaterialTheme(colorScheme = scheme(palette), typography = if (car) CarType else PhoneType, content = content)
    }
}
