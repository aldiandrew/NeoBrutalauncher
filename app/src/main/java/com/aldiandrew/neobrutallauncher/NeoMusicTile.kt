package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

private data class MusicApp(
    val label: String,
    val icon: android.graphics.drawable.Drawable,
    val packageName: String
)

private fun resolveMusicApp(context: Context): MusicApp? {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
    val info = context.packageManager
        .queryIntentActivities(intent, 0)
        .firstOrNull()
        ?.let { it.activityInfo.applicationInfo }
        ?: return null
    return MusicApp(
        label = context.packageManager.getApplicationLabel(info).toString(),
        icon = context.packageManager.getApplicationIcon(info),
        packageName = info.packageName
    )
}

private fun dispatchMediaKey(context: Context, keyCode: Int) {
    val audioManager = context.getSystemService(AudioManager::class.java)
    audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
}

@Composable
fun NeoMusicTile(context: Context, modifier: Modifier = Modifier) {
    val musicApp = remember { resolveMusicApp(context) }
    val textColor = MaterialTheme.colorScheme.onBackground
    val artBitmap = remember(musicApp?.packageName) {
        musicApp?.icon?.toBitmap(96, 96)?.asImageBitmap()
    }

    BrutalBlock(
        modifier = modifier,
        background = Color.Transparent,
        borderWidth = 3.dp,
        borderColor = textColor,
        shadowX = 0.dp,
        shadowY = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(66.dp),
                contentAlignment = Alignment.Center
            ) {
                if (artBitmap != null) {
                    Image(
                        bitmap = artBitmap,
                        contentDescription = musicApp?.label ?: "Music",
                        modifier = Modifier.size(62.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = textColor,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NOW PLAYING",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                Text(
                    text = musicApp?.label?.uppercase() ?: "SYSTEM MEDIA",
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicControlButton(
                    icon = Icons.Default.SkipPrevious,
                    description = "Previous",
                    textColor = textColor
                ) {
                    dispatchMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                }
                MusicControlButton(
                    icon = Icons.Default.PlayArrow,
                    description = "Play or pause",
                    textColor = textColor
                ) {
                    dispatchMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                }
                MusicControlButton(
                    icon = Icons.Default.SkipNext,
                    description = "Next",
                    textColor = textColor
                ) {
                    dispatchMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT)
                }
            }
        }
    }
}

@Composable
private fun MusicControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    textColor: Color,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(42.dp)
            .background(BrutalColors.Pink)
            .border(2.dp, BrutalColors.Ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = BrutalColors.Ink,
            modifier = Modifier.size(22.dp)
        )
    }
}
