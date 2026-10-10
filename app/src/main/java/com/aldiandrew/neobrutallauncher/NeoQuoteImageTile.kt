package com.aldiandrew.neobrutallauncher

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NeoQuoteImageTile(
    quote: String,
    imageUri: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, imageUri) {
        value = withContext(Dispatchers.IO) {
            imageUri?.takeIf { it.isNotBlank() }?.let {
                loadCustomWallpaper(context, it, maxDimensionOverride = 1200)
            }
        }
    }
    val grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

    BrutalBlock(
        modifier = modifier,
        background = BrutalColors.Ink,
        borderWidth = 4.dp,
        borderColor = BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = BrutalColors.Ink,
        contentPadding = 0.dp
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(BrutalColors.Ink)
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "QUOTE",
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Red
                )
                Spacer(Modifier.size(5.dp))
                Text(
                    text = "“$quote”",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Red,
                    maxLines = 7,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .border(3.dp, BrutalColors.Ink),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "User-selected grayscale image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = grayscale
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BrutalColors.White)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "IMAGE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink
                        )
                        Text(
                            text = "Long-press to choose an image",
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrutalColors.Ink,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
