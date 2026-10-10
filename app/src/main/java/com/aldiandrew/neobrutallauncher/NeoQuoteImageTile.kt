package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeoQuoteImageTile(
    quote: String,
    modifier: Modifier = Modifier
) {
    BrutalBlock(
        modifier = modifier,
        background = BrutalColors.White,
        borderWidth = 4.dp,
        borderColor = BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = BrutalColors.Ink,
        contentPadding = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrutalColors.White)
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "“$quote”",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 11.sp,
                lineHeight = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = BrutalColors.Red,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
