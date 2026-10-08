package com.aldiandrew.neobrutallauncher

import androidx.compose.runtime.staticCompositionLocalOf

enum class IconThemeStyle(val label: String) {
    ORIGINAL("ORIGINAL"),
    MONOCHROME("MONOCHROME"),
    ACCENT_TINTED("ACCENT TINTED"),
    TEXT_ONLY("TEXT ONLY"),
    CIRCLE("CIRCLE"),
    ROUNDED_SQUARE("ROUNDED SQUARE")
}

val LocalIconThemeStyle = staticCompositionLocalOf { IconThemeStyle.ORIGINAL }

