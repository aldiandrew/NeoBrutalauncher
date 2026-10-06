package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrutalBlock(
    modifier: Modifier = Modifier,
    background: Color,
    borderWidth: Dp = 3.dp,
    shadowX: Dp = 6.dp,
    shadowY: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shadowX, y = shadowY)
                .clip(RoundedCornerShape(0.dp))
                .background(BrutalColors.Ink)
        )
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(borderWidth, BrutalColors.Ink, RoundedCornerShape(0.dp))
                .background(background)
                .padding(10.dp),
            content = content
        )
    }
}
