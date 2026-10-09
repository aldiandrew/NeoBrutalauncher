package com.aldiandrew.neobrutallauncher

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.LocalTextStyle

object BrutalColors {
    val Ink = Color(0xFF111111)
    val Paper = Color(0xFFF4F0E6)
    val White = Color(0xFFFFFFFF)
    val Yellow = Color(0xFFFFE500)
    val Pink = Color(0xFFFF5C8A)
    val Cyan = Color(0xFF00D9FF)
    val DarkPaper = Color(0xFF121212)
    val DarkTile = Color(0xFF202020)
    val DarkWhite = Color(0xFFFFFFFF)
    val Red = Color(0xFFE00000)
    val Lime = Color(0xFFB8FF9F)
    val Lime = Color(0xFFB8FF9F)

    fun appPalette(seed: Int = 0): List<Color> {
        // Editorial neo-brutalism uses flat, opaque blocks with a controlled palette.
        // Keep the sequence predictable so adjacent tiles feel designed, not random.
        val base = listOf(
            White,
            Yellow,
            Pink,
            Cyan
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

private val DarkScheme = darkColorScheme(
    primary = BrutalColors.DarkWhite,
    onPrimary = BrutalColors.Ink,
    secondary = BrutalColors.Lime,
    onSecondary = BrutalColors.Ink,
    background = BrutalColors.DarkPaper,
    onBackground = BrutalColors.DarkWhite,
    surface = BrutalColors.DarkTile,
    onSurface = BrutalColors.DarkWhite,
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = BrutalColors.DarkWhite,
    outline = BrutalColors.DarkWhite,
    outlineVariant = Color(0xFF707070),
    error = BrutalColors.Red,
    onError = BrutalColors.White
)

@Composable
fun NeoBrutalTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    typographyStyle: TypographyStyle = TypographyStyle.DEFAULT,
    hideStatusBar: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val view = LocalView.current

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
        if (hideStatusBar) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    CompositionLocalProvider(
        LocalBrutalMetrics provides BrutalMetrics(),
        LocalBrutalTypographyStyle provides typographyStyle
    ) {
        MaterialTheme(colorScheme = if (isDark) DarkScheme else LightScheme) {
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
