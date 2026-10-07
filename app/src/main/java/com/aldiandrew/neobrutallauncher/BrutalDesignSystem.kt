package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BrutalityLevel(
    val label: String,
    val borderScale: Float,
    val shadowScale: Float
) {
    LITE("LITE", 0.75f, 0.65f),
    BRUTAL("BRUTAL", 1f, 1f),
    HARD("HARD", 1.35f, 1.5f),
    CHAOS("CHAOS", 1.65f, 2f)
}

enum class ClockStyle(val label: String) {
    POSTER("POSTER"),
    MONO("MONO"),
    CONDENSED("CONDENSED"),
    HUGE("HUGE")
}

data class BrutalMetrics(
    val borderScale: Float = 1f,
    val shadowScale: Float = 1f,
    val cornerRadius: Dp = 0.dp
)

val LocalBrutalMetrics = staticCompositionLocalOf { BrutalMetrics() }

object BrutalTypography {
    val Poster = FontFamily.SansSerif
    val Mono = FontFamily.Monospace
    val Black = FontWeight.Black
    val ExtraBold = FontWeight.ExtraBold
}

@Composable
fun BrutalLabel(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = BrutalColors.Yellow
) {
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(LocalBrutalMetrics.current.cornerRadius))
            .border(
                width = 2.dp * LocalBrutalMetrics.current.borderScale,
                color = BrutalColors.Ink,
                shape = RoundedCornerShape(LocalBrutalMetrics.current.cornerRadius)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            color = BrutalColors.Ink
        )
    }
}

@Composable
fun BrutalTape(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = BrutalColors.Pink
) {
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(2.dp))
            .border(
                width = 2.dp * LocalBrutalMetrics.current.borderScale,
                color = BrutalColors.Ink
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = BrutalColors.Ink
        )
    }
}

@Composable
fun BrutalStamp(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    background: Color = BrutalColors.Cyan
) {
    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(LocalBrutalMetrics.current.cornerRadius))
            .border(
                width = 3.dp * LocalBrutalMetrics.current.borderScale,
                color = BrutalColors.Ink,
                shape = RoundedCornerShape(LocalBrutalMetrics.current.cornerRadius)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = BrutalColors.Ink
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.7.sp,
            color = BrutalColors.Ink
        )
    }
}

@Composable
fun BrutalBar(
    value: Float,
    modifier: Modifier = Modifier,
    background: Color = BrutalColors.White,
    fill: Color = BrutalColors.Orange
) {
    val clamped = value.coerceIn(0f, 1f)
    BrutalBlock(
        modifier = modifier,
        background = background,
        borderWidth = 2.dp,
        shadowX = 3.dp,
        shadowY = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(clamped.coerceAtLeast(0.001f))
                    .height(14.dp)
                    .background(fill)
            )
            if (clamped < 1f) {
                Box(
                    modifier = Modifier
                        .weight((1f - clamped).coerceAtLeast(0.001f))
                        .height(14.dp)
                )
            }
        }
    }
}

@Composable
fun BrutalSection(
    title: String,
    modifier: Modifier = Modifier,
    background: Color = BrutalColors.Paper,
    content: @Composable ColumnScope.() -> Unit
) {
    BrutalBlock(
        modifier = modifier,
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        Column {
            BrutalLabel(text = title, background = BrutalColors.Yellow)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
