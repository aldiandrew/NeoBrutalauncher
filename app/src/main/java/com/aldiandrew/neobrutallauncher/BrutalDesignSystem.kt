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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
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
    val shadowScale: Float = 1f
)

val LocalBrutalMetrics = staticCompositionLocalOf { BrutalMetrics() }

object BrutalTypography {
    // Display: condensed, heavy headlines for tile headers and labels.
    val Display = FontFamily(
        Font(R.font.anton_regular, FontWeight.Normal)
    )

    // Body: geometric, highly readable text for descriptions and editable content.
    val Body = FontFamily(
        Font(R.font.space_grotesk_regular, FontWeight.Normal),
        Font(R.font.space_grotesk_bold, FontWeight.Bold)
    )

    val Bricolage = Body
    val Poster = Display
    val Mono = FontFamily.Monospace
    val Black = FontWeight.ExtraBold
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
            .background(background, RoundedCornerShape(0.dp))
            .border(
                width = 2.dp * LocalBrutalMetrics.current.borderScale,
                color = BrutalColors.Ink,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = BrutalTypography.Display,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
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
            .background(background, RoundedCornerShape(0.dp))
            .border(
                width = 2.dp * LocalBrutalMetrics.current.borderScale,
                color = BrutalColors.Ink,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontFamily = BrutalTypography.Display,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.sp,
            color = BrutalColors.Ink
        )
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
