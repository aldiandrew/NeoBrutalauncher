package com.aldiandrew.neobrutallauncher

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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
import kotlin.math.roundToInt

@Composable
fun BrutalWallpaper(
    uriString: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sourceKey = uriString ?: "__device_wallpaper__"

    val bitmap by produceState<Bitmap?>(initialValue = null, sourceKey) {
        value = withContext(Dispatchers.IO) {
            if (uriString.isNullOrBlank()) {
                loadDeviceWallpaper(context)
            } else {
                loadCustomWallpaper(context, uriString)
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
        val maxDimension = max(targetWidth, targetHeight) * 2

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

private fun loadDeviceWallpaper(context: android.content.Context): Bitmap? =
    runCatching {
        val targetWidth = context.resources.displayMetrics.widthPixels.coerceAtLeast(1)
        val targetHeight = context.resources.displayMetrics.heightPixels.coerceAtLeast(1)
        val drawable = WallpaperManager.getInstance(context).drawable
            ?: return@runCatching null

        drawableToBitmap(drawable, targetWidth, targetHeight)
    }.getOrNull()

private fun drawableToBitmap(
    drawable: Drawable,
    targetWidth: Int,
    targetHeight: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(
        targetWidth,
        targetHeight,
        Bitmap.Config.RGB_565
    )
    val canvas = Canvas(bitmap)

    val sourceWidth = drawable.intrinsicWidth.coerceAtLeast(1)
    val sourceHeight = drawable.intrinsicHeight.coerceAtLeast(1)
    val scale = max(
        targetWidth.toFloat() / sourceWidth,
        targetHeight.toFloat() / sourceHeight
    )

    val scaledWidth = (sourceWidth * scale).roundToInt()
    val scaledHeight = (sourceHeight * scale).roundToInt()
    val left = (targetWidth - scaledWidth) / 2
    val top = (targetHeight - scaledHeight) / 2

    drawable.setBounds(
        left,
        top,
        left + scaledWidth,
        top + scaledHeight
    )
    drawable.draw(canvas)
    return bitmap
}
