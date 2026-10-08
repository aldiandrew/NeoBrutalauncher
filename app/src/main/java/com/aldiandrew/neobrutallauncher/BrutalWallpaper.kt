package com.aldiandrew.neobrutallauncher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun BrutalWallpaper(
    uriString: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sourceKey = uriString ?: "__no_wallpaper__"

    val bitmap by produceState<Bitmap?>(initialValue = null, sourceKey) {
        value = withContext(Dispatchers.IO) {
            uriString?.takeIf { it.isNotBlank() }?.let {
                loadCustomWallpaper(context, it)
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

private fun loadCustomWallpaper(
    context: android.content.Context,
    uriString: String
): Bitmap? =
    runCatching {
        val targetWidth = context.resources.displayMetrics.widthPixels.coerceAtLeast(1)
        val targetHeight = context.resources.displayMetrics.heightPixels.coerceAtLeast(1)
        val maxDimension = max(targetWidth, targetHeight)

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > maxDimension ||
            bounds.outHeight / sampleSize > maxDimension
        ) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize.coerceAtLeast(1)
            inPreferredConfig = Bitmap.Config.RGB_565
        }

        context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }.getOrNull()
