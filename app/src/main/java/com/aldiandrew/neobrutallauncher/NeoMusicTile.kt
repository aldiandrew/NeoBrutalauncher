package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

private data class MusicApp(
    val label: String,
    val icon: android.graphics.drawable.Drawable,
    val packageName: String
)

private fun resolveMusicApp(context: Context): MusicApp? {
    for (packageName in NeoSupportedMusicApps.packages) {
        val info = runCatching {
            context.packageManager.getApplicationInfo(packageName, 0)
        }.getOrNull() ?: continue
        return MusicApp(
            label = context.packageManager.getApplicationLabel(info).toString(),
            icon = context.packageManager.getApplicationIcon(info),
            packageName = info.packageName
        )
    }
    return null
}

private fun dispatchMediaKey(context: Context, keyCode: Int) {
    val audioManager = context.getSystemService(AudioManager::class.java)
    audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
}

private fun hasMusicAccess(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

@Composable
fun NeoMusicTile(
    context: Context,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var hasAccess by remember { mutableStateOf(hasMusicAccess(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) hasAccess = hasMusicAccess(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val musicInfo by NeoMusicSessionStore.state.collectAsState()

    val preferences = remember { LauncherPreferences(context) }
    val fallbackApp = remember { resolveMusicApp(context) }
    val lastMusicPackage = remember(musicInfo?.packageName) {
        preferences.lastMusicPackage()?.takeIf { NeoSupportedMusicApps.supports(it) }
    }
    val musicPackage = musicInfo?.packageName
        ?.takeIf { NeoSupportedMusicApps.supports(it) }
        ?: lastMusicPackage
        ?: fallbackApp?.packageName
    val lastMusicLabel = remember(lastMusicPackage) {
        lastMusicPackage?.let {
            runCatching {
                val info = context.packageManager.getApplicationInfo(it, 0)
                context.packageManager.getApplicationLabel(info).toString()
            }.getOrNull()
        }
    }
    val musicLabel = musicInfo
        ?.takeIf { NeoSupportedMusicApps.supports(it.packageName) }
        ?.appLabel ?: lastMusicLabel ?: fallbackApp?.label ?: "SELECTED MUSIC PLAYER"
    val iconBitmap = remember(musicPackage) {
        runCatching {
            musicPackage?.let {
                context.packageManager.getApplicationIcon(it).toBitmap(96, 96).asImageBitmap()
            }
        }.getOrNull()
    }

    BrutalBlock(
        modifier = modifier.then(
            if (!hasAccess) Modifier.clickable {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else Modifier
        ),
        background = background,
        borderWidth = 4.dp,
        borderColor = if (background == BrutalColors.DarkTile || background == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = if (background == BrutalColors.DarkTile || background == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink
    ) {
        MusicTileContent(
            hasAccess = hasAccess,
            musicLabel = musicLabel,
            musicInfo = musicInfo?.takeIf { NeoSupportedMusicApps.supports(it.packageName) },
            iconBitmap = iconBitmap,
            textColor = textColor,
            context = context
        )
    }
}

@Composable
private fun MusicTileContent(
    hasAccess: Boolean,
    musicLabel: String,
    musicInfo: MusicInfo?,
    iconBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    textColor: Color,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(top = 6.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.Center
        ) {
            val albumArt = musicInfo?.albumArt
            if (albumArt != null) {
                Image(
                    bitmap = albumArt.asImageBitmap(),
                    contentDescription = "Album art",
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Crop
                )
            } else if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = musicLabel,
                    modifier = Modifier.size(52.dp)
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    "Music",
                    tint = textColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = if (hasAccess) "NOW PLAYING" else "MUSIC ACCESS NEEDED",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = musicLabel.uppercase(),
                fontSize = 12.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            musicInfo?.title?.takeIf { it.isNotBlank() }?.let { title ->
                val artist = musicInfo.artist?.takeIf { it.isNotBlank() }
                Text(
                    text = if (artist != null) "$title — $artist" else title,
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            musicInfo?.takeIf { it.durationMs > 0L }?.let { info ->
                MusicProgressIndicator(
                    musicInfo = info,
                    textColor = textColor
                )
            }
            if (!hasAccess) {
                Text(
                    text = "TAP TILE TO ALLOW MUSIC ACCESS",
                    fontSize = 6.sp,
                    lineHeight = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MusicControlButton(
                Icons.Default.SkipPrevious,
                "Previous",
                textColor,
                context,
                enabled = hasAccess && musicInfo != null
            )
            MusicControlButton(
                Icons.Default.PlayArrow,
                "Play or pause",
                textColor,
                context,
                enabled = hasAccess && musicInfo != null
            )
            MusicControlButton(
                Icons.Default.SkipNext,
                "Next",
                textColor,
                context,
                enabled = hasAccess && musicInfo != null
            )
        }
    }
}

@Composable
private fun MusicProgressIndicator(
    musicInfo: MusicInfo,
    textColor: Color
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var progressClock by remember(
        musicInfo.packageName,
        musicInfo.positionUpdatedAtMs,
        musicInfo.isPlaying
    ) {
        mutableLongStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(
        musicInfo.packageName,
        musicInfo.positionUpdatedAtMs,
        musicInfo.isPlaying,
        lifecycleOwner
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (musicInfo.isPlaying) {
                while (isActive) {
                    progressClock = System.currentTimeMillis()
                    delay(1000L)
                }
            } else {
                progressClock = System.currentTimeMillis()
            }
        }
    }

    val displayPositionMs = if (musicInfo.isPlaying) {
        musicInfo.positionMs +
            ((progressClock - musicInfo.positionUpdatedAtMs) * musicInfo.playbackSpeed).toLong()
    } else {
        musicInfo.positionMs
    }.coerceAtLeast(0L)

    val progress = (displayPositionMs.toFloat() / musicInfo.durationMs.toFloat())
        .coerceIn(0f, 1f)

    BrutalProgress(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        fillColor = textColor,
        trackColor = textColor.copy(alpha = 0.18f)
    )
}

@Composable
private fun MusicControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    textColor: Color,
    context: Context,
    enabled: Boolean
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(38.dp)
            .border(2.dp, textColor.copy(alpha = if (enabled) 1f else 0.45f))
            .clickable(enabled = enabled) {
            dispatchMediaKey(
                context,
                when (icon) {
                    Icons.Default.SkipPrevious -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
                    Icons.Default.SkipNext -> KeyEvent.KEYCODE_MEDIA_NEXT
                    else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                }
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            description,
            tint = textColor.copy(alpha = if (enabled) 1f else 0.45f),
            modifier = Modifier.size(20.dp)
        )
    }
}
