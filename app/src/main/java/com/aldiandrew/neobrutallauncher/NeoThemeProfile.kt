package com.aldiandrew.neobrutallauncher

import androidx.compose.ui.graphics.Color

enum class NeoThemeProfile(val label: String) {
    MONO("MONO"),
    BLUE("BLUE"),
    RED("RED"),
    GREEN("GREEN"),
    PURPLE("PURPLE"),
    ORANGE("ORANGE")
}

data class NeoThemePalette(
    val lightBackground: Color,
    val lightSurface: Color,
    val lightAccent: Color,
    val lightSecondary: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val darkAccent: Color,
    val darkSecondary: Color,
    val lightOnAccent: Color = BrutalColors.Ink,
    val darkOnAccent: Color = BrutalColors.White,
    val lightTilePalette: List<Color>,
    val darkTilePalette: List<Color>
) {
    fun background(dark: Boolean): Color = if (dark) darkBackground else lightBackground
    fun surface(dark: Boolean): Color = if (dark) darkSurface else lightSurface
    fun accent(dark: Boolean): Color = if (dark) darkAccent else lightAccent
    fun secondary(dark: Boolean): Color = if (dark) darkSecondary else lightSecondary
    fun onAccent(dark: Boolean): Color = if (dark) darkOnAccent else lightOnAccent
    fun tilePalette(dark: Boolean): List<Color> =
        if (dark) darkTilePalette else lightTilePalette
}

object NeoThemePalettes {
    val mono = NeoThemePalette(
        lightBackground = Color(0xFFF4F4F4),
        lightSurface = Color.White,
        lightAccent = Color(0xFF111111),
        lightSecondary = Color(0xFF666666),
        darkBackground = Color(0xFF111111),
        darkSurface = Color(0xFF242424),
        darkAccent = Color(0xFFF4F4F4),
        darkSecondary = Color(0xFFAAAAAA),
        lightOnAccent = Color.White,
        darkOnAccent = Color(0xFF111111),
        lightTilePalette = listOf(Color.White, Color(0xFFE6E6E6), Color(0xFFCCCCCC), Color(0xFFAAAAAA)),
        darkTilePalette = listOf(Color(0xFF242424), Color(0xFF303030), Color(0xFF3C3C3C))
    )

    val blue = NeoThemePalette(
        lightBackground = Color(0xFFEFF4FF),
        lightSurface = Color.White,
        lightAccent = Color(0xFF315EFF),
        lightSecondary = Color(0xFF76A0FF),
        darkBackground = Color(0xFF101827),
        darkSurface = Color(0xFF1D2A44),
        darkAccent = Color(0xFF6F91FF),
        darkSecondary = Color(0xFF9DB7FF),
        lightTilePalette = listOf(Color.White, Color(0xFFDDE6FF), Color(0xFFBFCFFF), Color(0xFFE8EEFF)),
        darkTilePalette = listOf(Color(0xFF1D2A44), Color(0xFF263A5E), Color(0xFF314A78))
    )

    val red = NeoThemePalette(
        lightBackground = Color(0xFFFFF0F0),
        lightSurface = Color.White,
        lightAccent = Color(0xFFD60000),
        lightSecondary = Color(0xFFFF6B6B),
        darkBackground = Color(0xFF1A1010),
        darkSurface = Color(0xFF2E1B1B),
        darkAccent = Color(0xFFFF5D5D),
        darkSecondary = Color(0xFFFF9B9B),
        lightTilePalette = listOf(Color.White, Color(0xFFFFDDDD), Color(0xFFFFBDBD), Color(0xFFFFEAEA)),
        darkTilePalette = listOf(Color(0xFF2E1B1B), Color(0xFF442323), Color(0xFF5A2B2B))
    )

    val green = NeoThemePalette(
        lightBackground = Color(0xFFF0FBF4),
        lightSurface = Color.White,
        lightAccent = Color(0xFF178A45),
        lightSecondary = Color(0xFF4BC477),
        darkBackground = Color(0xFF0F1913),
        darkSurface = Color(0xFF1D3025),
        darkAccent = Color(0xFF50D77E),
        darkSecondary = Color(0xFF83EBA3),
        lightTilePalette = listOf(Color.White, Color(0xFFDDF7E6), Color(0xFFB9EBC9), Color(0xFFEAFBF0)),
        darkTilePalette = listOf(Color(0xFF1D3025), Color(0xFF274633), Color(0xFF315842))
    )

    val purple = NeoThemePalette(
        lightBackground = Color(0xFFF7F2FF),
        lightSurface = Color.White,
        lightAccent = Color(0xFF7C38E5),
        lightSecondary = Color(0xFFAA72FF),
        darkBackground = Color(0xFF15101F),
        darkSurface = Color(0xFF281C38),
        darkAccent = Color(0xFFB37AFF),
        darkSecondary = Color(0xFFD0ADFF),
        lightTilePalette = listOf(Color.White, Color(0xFFE9DDFF), Color(0xFFD7BCFF), Color(0xFFF2EAFF)),
        darkTilePalette = listOf(Color(0xFF281C38), Color(0xFF37254D), Color(0xFF482F63))
    )

    val orange = NeoThemePalette(
        lightBackground = Color(0xFFFFF5EC),
        lightSurface = Color.White,
        lightAccent = Color(0xFFE45A00),
        lightSecondary = Color(0xFFFF934F),
        darkBackground = Color(0xFF1C140F),
        darkSurface = Color(0xFF332218),
        darkAccent = Color(0xFFFF8A3D),
        darkSecondary = Color(0xFFFFB276),
        lightTilePalette = listOf(Color.White, Color(0xFFFFE4D1), Color(0xFFFFCAA4), Color(0xFFFFF0E4)),
        darkTilePalette = listOf(Color(0xFF332218), Color(0xFF493021), Color(0xFF603D29))
    )

    fun forProfile(profile: NeoThemeProfile): NeoThemePalette =
        when (profile) {
            NeoThemeProfile.MONO -> mono
            NeoThemeProfile.BLUE -> blue
            NeoThemeProfile.RED -> red
            NeoThemeProfile.GREEN -> green
            NeoThemeProfile.PURPLE -> purple
            NeoThemeProfile.ORANGE -> orange
        }
}
