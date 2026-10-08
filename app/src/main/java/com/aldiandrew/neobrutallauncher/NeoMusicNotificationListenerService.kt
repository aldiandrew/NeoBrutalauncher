package com.aldiandrew.neobrutallauncher

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MusicInfo(
    val packageName: String,
    val appLabel: String,
    val title: String?,
    val artist: String?
)

object NeoMusicSessionStore {
    private val _state = MutableStateFlow<MusicInfo?>(null)
    val state = _state.asStateFlow()

    fun update(value: MusicInfo?) {
        _state.value = value
    }
}

class NeoMusicNotificationListenerService : NotificationListenerService() {
    private lateinit var sessionManager: MediaSessionManager
    private var currentController: MediaController? = null

    private val sessionListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            selectController(controllers ?: emptyList())
        }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            publish(currentController)
        }

        override fun onPlaybackStateChanged(state: android.media.session.PlaybackState?) {
            publish(currentController)
        }

        override fun onSessionDestroyed() {
            currentController = null
            NeoMusicSessionStore.update(null)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        NeoNotificationServiceRegistry.attach(this)
        NeoChatNotificationStore.setSelectedPackages(
            LauncherPreferences(this).chatNotificationPackages()
        )
        sessionManager = getSystemService(MediaSessionManager::class.java)
        sessionManager.addOnActiveSessionsChangedListener(
            sessionListener,
            ComponentName(this, NeoMusicNotificationListenerService::class.java)
        )
        selectController(
            sessionManager.getActiveSessions(
                ComponentName(this, NeoMusicNotificationListenerService::class.java)
            )
        )
    }

    override fun onListenerDisconnected() {
        NeoNotificationServiceRegistry.detach(this)
        if (::sessionManager.isInitialized) {
            runCatching { sessionManager.removeOnActiveSessionsChangedListener(sessionListener) }
        }
        currentController?.unregisterCallback(controllerCallback)
        currentController = null
        NeoMusicSessionStore.update(null)
        super.onListenerDisconnected()
    }



    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NeoChatNotificationStore.onPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NeoChatNotificationStore.onRemoved(sbn)
    }

    fun refreshChatNotifications() {
        NeoChatNotificationStore.clear()
        val active = runCatching { activeNotifications.orEmpty() }.getOrDefault(emptyArray())
        active.forEach(NeoChatNotificationStore::onPosted)
    }

    private fun selectController(controllers: List<MediaController>) {
        currentController?.unregisterCallback(controllerCallback)
        currentController = controllers.firstOrNull {
            it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull()

        currentController?.registerCallback(controllerCallback)
        publish(currentController)
    }

    private fun publish(controller: MediaController?) {
        if (controller == null) {
            NeoMusicSessionStore.update(null)
            return
        }

        val appInfo = runCatching {
            packageManager.getApplicationInfo(controller.packageName, 0)
        }.getOrNull() ?: return

        val label = packageManager.getApplicationLabel(appInfo).toString()
        val metadata = controller.metadata
        NeoMusicSessionStore.update(
            MusicInfo(
                packageName = controller.packageName,
                appLabel = label,
                title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            )
        )
    }
}
