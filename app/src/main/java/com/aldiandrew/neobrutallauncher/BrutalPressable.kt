package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrutalPressableBlock(
    modifier: Modifier = Modifier,
    background: androidx.compose.ui.graphics.Color,
    borderWidth: Dp = 4.dp,
    shadowX: Dp = 7.dp,
    shadowY: Dp = 7.dp,
    borderColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressedOffsetX = if (pressed) shadowX else 0.dp
    val pressedOffsetY = if (pressed) shadowY else 0.dp

    BrutalBlock(
        modifier = modifier
            .offset(x = pressedOffsetX, y = pressedOffsetY)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        background = background,
        borderWidth = borderWidth,
        shadowX = if (pressed) 0.dp else shadowX,
        shadowY = if (pressed) 0.dp else shadowY,
        borderColor = borderColor,
        content = content
    )
}
