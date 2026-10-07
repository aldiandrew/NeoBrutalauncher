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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrutalBlock(
    modifier: Modifier = Modifier,
    background: Color,
    borderWidth: Dp = 3.dp,
    shadowX: Dp = 6.dp,
    shadowY: Dp = 6.dp,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val style = LocalBrutalMetrics.current
    val shape = RoundedCornerShape(style.cornerRadius)
    val actualBorderWidth = (borderWidth * style.borderScale).coerceAtLeast(1.dp)
    val actualShadowX = shadowX * style.shadowScale
    val actualShadowY = shadowY * style.shadowScale
    val isDark = MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val resolvedShadowColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink
    val resolvedBorderColor = borderColor ?: BrutalColors.Ink

    Layout(
        modifier = modifier,
        content = {
            Box(
                modifier = Modifier.background(
                    color = resolvedShadowColor,
                    shape = shape
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = actualBorderWidth,
                        color = resolvedBorderColor,
                        shape = shape
                    )
                    .background(
                        color = background,
                        shape = shape
                    )
                    .padding(10.dp),
                content = content
            )
        }
    ) { measurables, constraints ->
        val contentPlaceable = measurables[1].measure(constraints)

        val shadowConstraints = Constraints.fixed(
            width = contentPlaceable.width,
            height = contentPlaceable.height
        )
        val shadowPlaceable = measurables[0].measure(shadowConstraints)

        layout(
            width = contentPlaceable.width,
            height = contentPlaceable.height
        ) {
            shadowPlaceable.place(
                x = actualShadowX.roundToPx(),
                y = actualShadowY.roundToPx()
            )
            contentPlaceable.place(
                x = 0,
                y = 0
            )
        }
    }
}
