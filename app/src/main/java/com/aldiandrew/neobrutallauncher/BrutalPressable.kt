package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrutalPressableBlock(
    modifier: Modifier = Modifier,
    background: androidx.compose.ui.graphics.Color,
    borderWidth: Dp = NeoBrutalTokens.Border.Primary,
    shadowX: Dp = NeoBrutalTokens.Shadow.Medium,
    shadowY: Dp = NeoBrutalTokens.Shadow.Medium,
    borderColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val motionConfig = LocalNeoMotionConfig.current
    val pressedOffsetX by animateDpAsState(
        targetValue = if (pressed && !motionConfig.reduceMotion) shadowX else if (pressed) shadowX else 0.dp,
        animationSpec = if (motionConfig.reduceMotion) {
            androidx.compose.animation.core.tween(durationMillis = 1)
        } else {
            motionConfig.springSpec()
        },
        label = "brutal-press-x"
    )
    val pressedOffsetY by animateDpAsState(
        targetValue = if (pressed && !motionConfig.reduceMotion) shadowY else if (pressed) shadowY else 0.dp,
        animationSpec = if (motionConfig.reduceMotion) {
            androidx.compose.animation.core.tween(durationMillis = 1)
        } else {
            motionConfig.springSpec()
        },
        label = "brutal-press-y"
    )

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
        shadowX = if (pressed) NeoBrutalTokens.Shadow.None else shadowX,
        shadowY = if (pressed) NeoBrutalTokens.Shadow.None else shadowY,
        borderColor = borderColor,
        content = content
    )
}
