package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap

fun IconThemeStyle.showsIcon(): Boolean = this != IconThemeStyle.TEXT_ONLY

@Composable
fun NeoAppIcon(
    app: AppInfo,
    size: Dp,
    style: IconThemeStyle,
    modifier: Modifier = Modifier
) {
    if (!style.showsIcon()) return

    val bitmap = remember(app.packageName, app.activityName, app.icon, size) {
        app.icon.toBitmap(96, 96).asImageBitmap()
    }
    val isDark = LocalNeoThemeIsDark.current
    val palette = LocalNeoThemePalette.current

    val iconModifier = modifier
        .size(size)
        .then(
            when (style) {
                IconThemeStyle.ORIGINAL,
                IconThemeStyle.MONOCHROME,
                IconThemeStyle.ACCENT_TINTED -> Modifier
                IconThemeStyle.CIRCLE -> Modifier.clip(CircleShape)
                IconThemeStyle.ROUNDED_SQUARE -> Modifier.clip(RoundedCornerShape(12.dp))
                IconThemeStyle.TEXT_ONLY -> Modifier
            }
        )

    val tint = when (style) {
        IconThemeStyle.ORIGINAL,
        IconThemeStyle.CIRCLE,
        IconThemeStyle.ROUNDED_SQUARE,
        IconThemeStyle.TEXT_ONLY -> null
        IconThemeStyle.MONOCHROME -> MaterialTheme.colorScheme.onBackground
        IconThemeStyle.ACCENT_TINTED -> palette.accent(isDark)
    }

    Image(
        bitmap = bitmap,
        contentDescription = app.label,
        modifier = iconModifier,
        colorFilter = tint?.let { ColorFilter.tint(it, BlendMode.SrcIn) }
    )
}
