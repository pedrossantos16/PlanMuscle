package com.pedro.planmuscle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeOption(val title: String, val isDark: Boolean) {
    NEON_FITNESS("Neon Fitness (Escuro)", true),
    EMERALD_POWER("Esmeralda (Escuro)", true),
    SUNSET_VIBRANT("Sunset Orange (Escuro)", true),
    ROYAL_PURPLE("Royal Violet (Escuro)", true),
    LIGHT_MINT("Menta Clear (Claro)", false),
    LIGHT_CORAL("Coral Bright (Claro)", false),
    LIGHT_OCEAN("Ocean Breeze (Claro)", false),
    LIGHT_LAVENDER("Lavender Pure (Claro)", false)
}

// --- DARK COLOR SCHEMES ---
private val NeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    onPrimary = NeonOnPrimary,
    primaryContainer = NeonPrimaryContainer,
    onPrimaryContainer = NeonOnPrimaryContainer,
    secondary = NeonSecondary,
    tertiary = NeonTertiary,
    background = NeonBackground,
    surface = NeonSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB3B3B3)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = EmeraldSecondary,
    tertiary = EmeraldTertiary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = SunsetOnPrimary,
    primaryContainer = SunsetPrimaryContainer,
    onPrimaryContainer = SunsetOnPrimaryContainer,
    secondary = SunsetSecondary,
    tertiary = SunsetTertiary,
    background = SunsetBackground,
    surface = SunsetSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3D2A1D),
    onSurfaceVariant = Color(0xFFD4BBAA)
)

private val PurpleColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurpleOnPrimaryContainer,
    secondary = PurpleSecondary,
    tertiary = PurpleTertiary,
    background = PurpleBackground,
    surface = PurpleSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2E1B42),
    onSurfaceVariant = Color(0xFFC7B8DA)
)

// --- LIGHT COLOR SCHEMES ---
private val LightMintColorScheme = lightColorScheme(
    primary = LightMintPrimary,
    onPrimary = LightMintOnPrimary,
    primaryContainer = LightMintPrimaryContainer,
    onPrimaryContainer = LightMintOnPrimaryContainer,
    secondary = LightMintSecondary,
    background = LightMintBackground,
    surface = LightMintSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

private val LightCoralColorScheme = lightColorScheme(
    primary = LightCoralPrimary,
    onPrimary = LightCoralOnPrimary,
    primaryContainer = LightCoralPrimaryContainer,
    onPrimaryContainer = LightCoralOnPrimaryContainer,
    secondary = LightCoralSecondary,
    background = LightCoralBackground,
    surface = LightCoralSurface,
    onBackground = Color(0xFF1C1917),
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFF5EBE6),
    onSurfaceVariant = Color(0xFF57534E)
)

private val LightOceanColorScheme = lightColorScheme(
    primary = LightOceanPrimary,
    onPrimary = LightOceanOnPrimary,
    primaryContainer = LightOceanPrimaryContainer,
    onPrimaryContainer = LightOceanOnPrimaryContainer,
    secondary = LightOceanSecondary,
    background = LightOceanBackground,
    surface = LightOceanSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE0F2FE),
    onSurfaceVariant = Color(0xFF334155)
)

private val LightLavenderColorScheme = lightColorScheme(
    primary = LightLavenderPrimary,
    onPrimary = LightLavenderOnPrimary,
    primaryContainer = LightLavenderPrimaryContainer,
    onPrimaryContainer = LightLavenderOnPrimaryContainer,
    secondary = LightLavenderSecondary,
    background = LightLavenderBackground,
    surface = LightLavenderSurface,
    onBackground = Color(0xFF1E1B4B),
    onSurface = Color(0xFF1E1B4B),
    surfaceVariant = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFF4C1D95)
)

@Composable
fun PlanMuscleTheme(
    themeOption: AppThemeOption = AppThemeOption.NEON_FITNESS,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeOption) {
        AppThemeOption.NEON_FITNESS -> NeonColorScheme
        AppThemeOption.EMERALD_POWER -> EmeraldColorScheme
        AppThemeOption.SUNSET_VIBRANT -> SunsetColorScheme
        AppThemeOption.ROYAL_PURPLE -> PurpleColorScheme
        AppThemeOption.LIGHT_MINT -> LightMintColorScheme
        AppThemeOption.LIGHT_CORAL -> LightCoralColorScheme
        AppThemeOption.LIGHT_OCEAN -> LightOceanColorScheme
        AppThemeOption.LIGHT_LAVENDER -> LightLavenderColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
