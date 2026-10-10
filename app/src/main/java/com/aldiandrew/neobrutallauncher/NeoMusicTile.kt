package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
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
import java.util.Locale

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

private fun hasMusicAccess(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

private fun openMusicPlayer(context: Context, packageName: String?) {
    val launchIntent = packageName?.let {
        runCatching { context.packageManager.getLaunchIntentForPackage(it) }.getOrNull()
    }
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(launchIntent) }
    }
}

@Composable
fun NeoMusicTile(
    context: Context,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
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
    // Use the actual active media session first, including players not in the legacy allowlist.
    val musicPackage = musicInfo?.packageName
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
    val supportedInfo = musicInfo
    val musicLabel = supportedInfo?.appLabel ?: lastMusicLabel ?: fallbackApp?.label ?: "SELECT MUSIC PLAYER"
    val iconBitmap = remember(musicPackage) {
        runCatching {
            musicPackage?.let {
                context.packageManager.getApplicationIcon(it).toBitmap(96, 96).asImageBitmap()
            }
        }.getOrNull()
    }

    BrutalBlock(
        modifier = modifier.clickable {
            if (!hasAccess) {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                openMusicPlayer(context, musicPackage)
            }
        },
        background = background,
        borderWidth = 4.dp,
        contentPadding = 0.dp,
        borderColor = if (background == BrutalColors.DarkTile || background == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = if (background == BrutalColors.DarkTile || background == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink
    ) {
        MusicTileContent(
            hasAccess = hasAccess,
            musicLabel = musicLabel,
            musicInfo = supportedInfo,
            iconBitmap = iconBitmap,
            textColor = textColor,
            context = context,
            musicPackage = musicPackage
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
    context: Context,
    musicPackage: String?
) {
    val albumArt = musicInfo?.albumArt?.takeUnless { it.isRecycled }
    val title = musicInfo?.title?.takeIf { it.isNotBlank() } ?: musicLabel
    val artist = musicInfo?.artist?.takeIf { it.isNotBlank() }
    val overlayColor = Color(0xE6111111)
    val controlColor = BrutalColors.White

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (textColor == BrutalColors.DarkWhite) BrutalColors.DarkTile else BrutalColors.Pink)
    ) {
        if (albumArt != null) {
            Image(
                bitmap = albumArt.asImageBitmap(),
                contentDescription = "Album art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (textColor == BrutalColors.DarkWhite) BrutalColors.DarkTile else BrutalColors.Cyan),
                contentAlignment = Alignment.Center
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = musicLabel,
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = controlColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Text(
            text = "MUSIC",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(7.dp)
                .background(BrutalColors.Yellow)
                .padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 8.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.Black,
            color = BrutalColors.Ink,
            maxLines = 1
        )

        if (maxWidth < 160.dp || maxHeight < 72.dp) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                MusicControlButton(
                    if (musicInfo?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (musicInfo?.isPlaying == true) "Pause" else "Play",
                    controlColor,
                    enabled = hasAccess && musicPackage != null,
                    onClick = {
                        if (musicInfo == null) openMusicPlayer(context, musicPackage)
                        else NeoNotificationServiceRegistry.service?.controlPlayback("toggle")
                    }
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(overlayColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (hasAccess) title.uppercase(Locale.getDefault()) else "ENABLE MUSIC ACCESS",
                        fontSize = 10.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = controlColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist?.uppercase(Locale.getDefault()) ?: musicLabel.uppercase(Locale.getDefault()),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = controlColor.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    musicInfo?.takeIf { it.durationMs > 0L }?.let { info ->
                        MusicProgressIndicator(musicInfo = info, textColor = controlColor)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MusicControlButton(
                        Icons.Default.SkipPrevious,
                        "Previous",
                        controlColor,
                        enabled = hasAccess && musicInfo != null,
                        onClick = { NeoNotificationServiceRegistry.service?.controlPlayback("previous") }
                    )
                    MusicControlButton(
                        if (musicInfo?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (musicInfo?.isPlaying == true) "Pause" else "Play",
                        controlColor,
                        enabled = hasAccess && musicPackage != null,
                        onClick = {
                            if (musicInfo == null) openMusicPlayer(context, musicPackage)
                            else NeoNotificationServiceRegistry.service?.controlPlayback("toggle")
                        }
                    )
                    MusicControlButton(
                        Icons.Default.SkipNext,
                        "Next",
                        controlColor,
                        enabled = hasAccess && musicInfo != null,
                        onClick = { NeoNotificationServiceRegistry.service?.controlPlayback("next") }
                    )
                }
            }
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
                    delay(2000L)
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
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        fillColor = if (textColor == BrutalColors.DarkWhite) BrutalColors.Lime else textColor,
        trackColor = textColor.copy(alpha = 0.18f)
    )
}

@Composable
private fun MusicControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(30.dp)
            .border(2.dp, textColor.copy(alpha = if (enabled) 1f else 0.45f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            description,
            tint = textColor.copy(alpha = if (enabled) 1f else 0.45f),
            modifier = Modifier.size(16.dp)
        )
    }
}
