package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

data class NeoChatNotification(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postedTime: Long
)

object NeoChatNotificationStore {
    private val selectedPackages = linkedSetOf<String>()
    private val latestByPackage = linkedMapOf<String, NeoChatNotification>()
    private val _state = MutableStateFlow<List<NeoChatNotification>>(emptyList())
    val state = _state.asStateFlow()

    @Synchronized
    fun setSelectedPackages(packages: List<String>) {
        selectedPackages.clear()
        selectedPackages.addAll(packages.filter { it.isNotBlank() }.distinct().take(2))
        latestByPackage.keys.retainAll(selectedPackages)
        publish()
    }

    @Synchronized
    fun clear() {
        latestByPackage.clear()
        publish()
    }

    @Synchronized
    fun onPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in selectedPackages) return
        if ((sbn.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0) return

        val extras = sbn.notification.extras
        val title = (
            extras.getCharSequence(android.app.Notification.EXTRA_TITLE)
                ?: extras.getCharSequence(android.app.Notification.EXTRA_CONVERSATION_TITLE)
                ?: ""
            ).toString().trim()
        val text = (
            extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(android.app.Notification.EXTRA_TEXT)
                ?: ""
            ).toString().trim()

        if (title.isBlank() && text.isBlank()) return

        latestByPackage[sbn.packageName] = NeoChatNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            appLabel = sbn.sbnAppLabel(),
            title = title.ifBlank { sbn.sbnAppLabel() },
            text = text,
            postedTime = sbn.postTime
        )
        publish()
    }

    @Synchronized
    fun onRemoved(sbn: StatusBarNotification, replacement: StatusBarNotification? = null) {
        val current = latestByPackage[sbn.packageName]
        if (current?.key != sbn.key) return

        if (replacement != null && replacement.packageName == sbn.packageName) {
            onPosted(replacement)
        } else {
            latestByPackage.remove(sbn.packageName)
            publish()
        }
    }

    @Synchronized
    private fun publish() {
        _state.value = latestByPackage.values.sortedByDescending { it.postedTime }
    }

    private fun StatusBarNotification.sbnAppLabel(): String =
        runCatching {
            val info = NeoNotificationServiceRegistry.service?.packageManager?.getApplicationInfo(packageName, 0)
            if (info != null) {
                NeoNotificationServiceRegistry.service?.packageManager
                    ?.getApplicationLabel(info)
                    ?.toString()
            } else {
                null
            }
        }.getOrNull() ?: packageName
}

object NeoNotificationServiceRegistry {
    private var serviceRef = WeakReference<NeoMusicNotificationListenerService>(null)

    val service: NeoMusicNotificationListenerService?
        get() = serviceRef.get()

    fun attach(service: NeoMusicNotificationListenerService) {
        serviceRef = WeakReference(service)
    }

    fun detach(service: NeoMusicNotificationListenerService) {
        if (serviceRef.get() === service) {
            serviceRef.clear()
        }
    }
}

@Composable
fun NeoChatNotificationTile(
    context: Context,
    packageName: String?,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val notifications by NeoChatNotificationStore.state.collectAsState()
    val item = notifications.firstOrNull { it.packageName == packageName }
    val packageManager = context.packageManager

    val appLabel = packageName?.let {
        runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(it, 0)
            ).toString()
        }.getOrNull()
    } ?: "CHAT"

    val iconBitmap = packageName?.let {
        runCatching {
            packageManager.getApplicationIcon(it).toBitmap(72, 72).asImageBitmap()
        }.getOrNull()
    }

    BrutalBlock(
        modifier = modifier.clickable {
            packageName?.let {
                packageManager.getLaunchIntentForPackage(it)?.let { launchIntent ->
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
            }
        },
        background = background,
        borderWidth = 3.dp,
        borderColor = if (background == BrutalColors.DarkTile) BrutalColors.DarkWhite else BrutalColors.Ink,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        if (iconBitmap != null) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = appLabel,
                    modifier = Modifier.size(38.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = appLabel.uppercase(),
                        fontFamily = BrutalTypography.Display,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item?.title ?: "NO NEW NOTIFICATIONS",
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item != null && item.text.isNotBlank()) {
                        Text(
                            text = item.text,
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat notifications",
                    tint = textColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = "NO NEW NOTIFICATIONS",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
