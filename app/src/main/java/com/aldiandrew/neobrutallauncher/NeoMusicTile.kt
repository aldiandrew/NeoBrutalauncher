package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.IconButton
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
        shadowX = 6.dp,
        shadowY = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(86.dp)
                    .background(BrutalColors.Pink)
                    .border(3.dp, BrutalColors.Ink),
                contentAlignment = Alignment.Center
            ) {
                if (artBitmap != null) {
                    Image(
                        bitmap = artBitmap,
                        contentDescription = musicApp?.label ?: "Music",
                        modifier = Modifier.size(70.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = BrutalColors.Ink,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NOW PLAYING",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
                Text(
                    text = musicApp?.label?.uppercase() ?: "SYSTEM MEDIA",
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "MEDIA CONTROLS FOLLOW THE ACTIVE PLAYER",
                    fontSize = 7.sp,
                    lineHeight = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.72f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { dispatchMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, "Previous", tint = textColor)
                    }
                    IconButton(
                        onClick = { dispatchMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Play or pause", tint = textColor)
                    }
                    IconButton(
                        onClick = { dispatchMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, "Next", tint = textColor)
                    }
                }
            }
        }
    }
}
