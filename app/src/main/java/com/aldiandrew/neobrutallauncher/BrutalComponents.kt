package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BrutalBlock(
    modifier: Modifier = Modifier,
    background: Color,
    borderWidth: Dp = NeoBrutalTokens.Border.Primary,
    shadowX: Dp = NeoBrutalTokens.Shadow.Medium,
    shadowY: Dp = NeoBrutalTokens.Shadow.Medium,
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
    val darkSurface = background == BrutalColors.DarkPaper ||
        background == BrutalColors.DarkTile ||
        background == BrutalColors.Ink
    val resolvedShadowColor = if (shadowEnabled) {
        shadowColor ?: if (darkSurface) BrutalColors.DarkWhite else BrutalColors.Ink
    } else {
        Color.Transparent
    }
    val resolvedBorderColor = borderColor ?: if (darkSurface) {
        BrutalColors.DarkWhite
    } else {
        BrutalColors.Ink
    }

    Layout(
        modifier = modifier,
        content = {
            Box(modifier = Modifier.background(color = resolvedShadowColor, shape = shape))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(actualBorderWidth, resolvedBorderColor, shape)
                    .background(background, shape)
                    .padding(NeoBrutalTokens.Spacing.Small),
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

@Composable
fun BrutalToggle(
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 56.dp, height = 30.dp)
            .border(NeoBrutalTokens.Border.Primary, BrutalColors.Ink, RoundedCornerShape(0.dp))
            .background(
                if (checked) accent else BrutalColors.White,
                RoundedCornerShape(0.dp)
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(NeoBrutalTokens.Spacing.Micro)
                .size(22.dp)
                .border(NeoBrutalTokens.Border.Secondary, BrutalColors.Ink, RoundedCornerShape(0.dp))
                .background(BrutalColors.Ink, RoundedCornerShape(0.dp))
        )
    }
}

@Composable
fun BrutalCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    accent: Color = BrutalColors.Yellow
) {
    Box(
        modifier = modifier
            .size(size)
            .border(3.dp, BrutalColors.Ink, RoundedCornerShape(0.dp))
            .background(
                if (checked) accent else BrutalColors.White,
                RoundedCornerShape(0.dp)
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Text(
                text = "✓",
                fontSize = NeoBrutalTokens.Type.Label,
                fontWeight = FontWeight.Black,
                color = BrutalColors.Ink
            )
        }
    }
}

@Composable
fun BrutalProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = BrutalColors.Yellow,
    trackColor: Color = BrutalColors.White
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(12.dp)
            .border(3.dp, BrutalColors.Ink, RoundedCornerShape(0.dp))
            .background(trackColor, RoundedCornerShape(0.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .fillMaxHeight()
                .background(fillColor, RoundedCornerShape(0.dp))
        )
    }
}
