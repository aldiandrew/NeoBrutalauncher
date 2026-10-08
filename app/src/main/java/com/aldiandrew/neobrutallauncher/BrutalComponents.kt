package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrutalBlock(
    modifier: Modifier = Modifier,
    background: Color,
    borderWidth: Dp = 4.dp,
    shadowX: Dp = 7.dp,
    shadowY: Dp = 7.dp,
    borderColor: Color? = null,
    shadowColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val style = LocalBrutalMetrics.current
    val shape = RoundedCornerShape(0.dp)
    val actualBorderWidth = (borderWidth * style.borderScale).coerceAtLeast(1.dp)
    val actualShadowX = shadowX * style.shadowScale
    val actualShadowY = shadowY * style.shadowScale
    val shadowEnabled = actualShadowX != 0.dp || actualShadowY != 0.dp
    val resolvedShadowColor = if (shadowEnabled) {
        shadowColor ?: BrutalColors.Ink
    } else {
        Color.Transparent
    }
    val resolvedBorderColor = borderColor ?: BrutalColors.Ink

    Layout(
        modifier = modifier,
        content = {
            Box(modifier = Modifier.background(color = resolvedShadowColor, shape = shape))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(actualBorderWidth, resolvedBorderColor, shape)
                    .background(background, shape)
                    .padding(10.dp),
                content = content
            )
        }
    ) { measurables, constraints ->
        val contentPlaceable = measurables[1].measure(constraints)
        val shadowPlaceable = measurables[0].measure(
            Constraints.fixed(contentPlaceable.width, contentPlaceable.height)
        )
        layout(contentPlaceable.width, contentPlaceable.height) {
            shadowPlaceable.place(actualShadowX.roundToPx(), actualShadowY.roundToPx())
            contentPlaceable.place(0, 0)
        }
    }
}
