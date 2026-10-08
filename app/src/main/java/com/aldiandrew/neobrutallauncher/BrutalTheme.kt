package com.aldiandrew.neobrutallauncher

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.LocalTextStyle

object BrutalColors {
    val Ink = Color(0xFF111111)
    val Paper = Color(0xFFF4F0E6)
    val White = Color(0xFFFFFFFF)
    val Yellow = Color(0xFFFFE500)
    val Pink = Color(0xFFFF5C8A)
    val Cyan = Color(0xFF00D9FF)
    val Lime = Color(0xFF9BFF00)
    val Orange = Color(0xFFFF6B00)
    val Purple = Color(0xFF9B5CFF)
    val Peach = Color(0xFFF4B69C)
    val Lavender = Color(0xFFD4D1FA)
    val Sky = Color(0xFFD1E3FA)
    val Mint = Color(0xFFD1FAF0)
    val DarkPaper = Color(0xFF171717)
    val DarkTile = Color(0xFF292929)
    val DarkWhite = Color(0xFFF7F7F7)
    val Red = Color(0xFFE00000)

    fun appPalette(seed: Int = 0): List<Color> {
        // Editorial neo-brutalism uses flat, opaque blocks with a controlled palette.
        // Keep the sequence predictable so adjacent tiles feel designed, not random.
        val base = listOf(
            White,
            Yellow,
            Pink,
            Cyan,
            Peach,
            Mint,
            Lavender,
            Sky
        )
        val shift = Math.floorMod(seed, base.size)
        return List(base.size) { index -> base[(index + shift) % base.size] }
    }
}

private val LightScheme = lightColorScheme(
    primary = BrutalColors.Ink,
    onPrimary = BrutalColors.White,
    secondary = BrutalColors.Pink,
    onSecondary = BrutalColors.Ink,
    background = BrutalColors.Paper,
    onBackground = BrutalColors.Ink,
    surface = BrutalColors.Paper,
    onSurface = BrutalColors.Ink
)

private val DefaultLightScheme = lightColorScheme(
    primary = BrutalColors.Ink,
    onPrimary = BrutalColors.White,
    secondary = BrutalColors.Pink,
    onSecondary = BrutalColors.Ink,
    background = BrutalColors.Paper,
    onBackground = BrutalColors.Ink,
    surface = BrutalColors.Paper,
    onSurface = BrutalColors.Ink
)

private val DefaultDarkScheme = darkColorScheme(
    primary = BrutalColors.DarkWhite,
    onPrimary = BrutalColors.Ink,
    secondary = BrutalColors.Cyan,
    onSecondary = BrutalColors.Ink,
    background = BrutalColors.DarkPaper,
    onBackground = BrutalColors.DarkWhite,
    surface = BrutalColors.DarkTile,
    onSurface = BrutalColors.DarkWhite,
    surfaceVariant = BrutalColors.Ink,
    onSurfaceVariant = BrutalColors.DarkWhite,
    outline = BrutalColors.DarkWhite,
    error = BrutalColors.Red,
    onError = BrutalColors.White
)

val LocalNeoThemePalette = staticCompositionLocalOf { NeoThemePalettes.forProfile(NeoThemeProfile.MONO) }

@Composable
fun NeoBrutalTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    themeProfile: NeoThemeProfile = NeoThemeProfile.MONO,
    typographyStyle: TypographyStyle = TypographyStyle.POSTER,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val palette = NeoThemePalettes.forProfile(themeProfile)
    val scheme = if (isDark) {
        darkColorScheme(
            primary = palette.darkAccent,
            onPrimary = palette.darkOnAccent,
            secondary = palette.darkSecondary,
            onSecondary = palette.darkOnAccent,
            background = palette.darkBackground,
            onBackground = BrutalColors.DarkWhite,
            surface = palette.darkSurface,
            onSurface = BrutalColors.DarkWhite,
            surfaceVariant = palette.darkSurface,
            onSurfaceVariant = BrutalColors.DarkWhite,
            outline = BrutalColors.DarkWhite,
            error = BrutalColors.Red,
            onError = BrutalColors.White
        )
    } else {
        lightColorScheme(
            primary = palette.lightAccent,
            onPrimary = palette.lightOnAccent,
            secondary = palette.lightSecondary,
            onSecondary = palette.lightOnAccent,
            background = palette.lightBackground,
            onBackground = BrutalColors.Ink,
            surface = palette.lightSurface,
            onSurface = BrutalColors.Ink,
            surfaceVariant = palette.lightSurface,
            onSurfaceVariant = BrutalColors.Ink,
            outline = BrutalColors.Ink,
            error = BrutalColors.Red,
            onError = BrutalColors.White
        )
    }

    val view = LocalView.current

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
    }

    CompositionLocalProvider(
        LocalBrutalMetrics provides BrutalMetrics(),
        LocalBrutalTypographyStyle provides typographyStyle,
        LocalNeoThemePalette provides palette
    ) {
        MaterialTheme(colorScheme = scheme) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(
                    fontFamily = BrutalTypography.Body
                )
            ) {
                content()
            }
        }
    }
}
