package com.aldiandrew.neobrutallauncher

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single source of truth for the core Neo-Brutalist visual rhythm.
 *
 * The launcher is intentionally tighter than a web marketing page because
 * this is a dense mobile home surface, but it follows the guide's core
 * 8-point spacing rhythm and hard-edged 2–4dp borders / 4–8dp shadows.
 */
object NeoBrutalTokens {
    object Spacing {
        val Micro: Dp = 4.dp
        val Small: Dp = 8.dp
        val Medium: Dp = 16.dp
        val Large: Dp = 24.dp
        val XLarge: Dp = 32.dp
    }

    object Border {
        val Secondary: Dp = 2.dp
        val Primary: Dp = 3.dp
        val Strong: Dp = 4.dp
    }

    object Shadow {
        val Small: Dp = 4.dp
        val Medium: Dp = 6.dp
        val Large: Dp = 8.dp
        val None: Dp = 0.dp
    }

    object Type {
        val Label: androidx.compose.ui.unit.TextUnit = 10.sp
        val Body: androidx.compose.ui.unit.TextUnit = 14.sp
        val Title: androidx.compose.ui.unit.TextUnit = 24.sp
        val Hero: androidx.compose.ui.unit.TextUnit = 42.sp
    }
}
