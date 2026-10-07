package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val DarkPaper = Color(0xFF171717)
    val DarkTile = Color(0xFF292929)
    val DarkWhite = Color(0xFFF7F7F7)

    fun appPalette(seed: Int = 0): List<Color> {
        val base = listOf(
            Pink,
            Cyan,
            Lime,
            Orange,
            Purple,
            White,
            Yellow,
            Pink
        )
        val shift = Math.floorMod(seed, base.size)
        return List(base.size) { index ->
            base[(index + shift) % base.size]
        }
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

private val DarkScheme = darkColorScheme(
    primary = BrutalColors.DarkWhite,
    onPrimary = BrutalColors.Ink,
    secondary = BrutalColors.Cyan,
    onSecondary = BrutalColors.Ink,
    background = BrutalColors.DarkPaper,
    onBackground = BrutalColors.DarkWhite,
    surface = BrutalColors.DarkPaper,
    onSurface = BrutalColors.DarkWhite
)

@Composable
fun NeoBrutalTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    brutalityLevel: BrutalityLevel = BrutalityLevel.BRUTAL,
    cornerRadius: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    CompositionLocalProvider(
        LocalBrutalMetrics provides BrutalMetrics(
            borderScale = brutalityLevel.borderScale,
            shadowScale = brutalityLevel.shadowScale,
            cornerRadius = cornerRadius.coerceIn(0.dp, 16.dp)
        )
    ) {
        MaterialTheme(
            colorScheme = if (isDark) DarkScheme else LightScheme,
            content = content
        )
    }
}
