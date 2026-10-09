package com.aldiandrew.neobrutallauncher

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MusicInfo(
    val packageName: String,
    val appLabel: String,
    val title: String?,
    val artist: String?,
    val albumArt: Bitmap?,
    val positionMs: Long,
    val durationMs: Long,
    val playbackSpeed: Float,
    val positionUpdatedAtMs: Long,
    val isPlaying: Boolean
)

object NeoSupportedMusicApps {
    // Only known music-player packages are allowed to own the music tile.
    // This prevents unrelated media sessions (for example PDF readers) from taking it over.
    val packages = linkedSetOf(
        "com.google.android.apps.youtube.music",
        "com.spotify.music",
        "com.google.android.music",
        "com.android.music",
        "com.motorola.music",
        "com.sec.android.app.music",
        "com.miui.player",
        "com.oppo.music",
        "com.vivo.music",
        "com.huawei.music",
        "com.oneplus.music",
        "org.videolan.vlc",
        "com.soundcloud.android"
    )

    fun supports(packageName: String?): Boolean = packageName != null && packageName in packages
}

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
    private val preferences by lazy { LauncherPreferences(applicationContext) }
    private var cachedAlbumArtKey: String? = null
    private var cachedAlbumArt: Bitmap? = null

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
        refreshChatNotifications()
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
        clearAlbumArtCache()
        NeoMusicSessionStore.update(null)
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NeoChatNotificationStore.onPosted(this, sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NeoChatNotificationStore.onRemoved(sbn)
        refreshChatNotifications()
    }

    fun refreshChatNotifications() {
        NeoChatNotificationStore.clear()
        runCatching { activeNotifications.orEmpty() }
            .getOrDefault(emptyArray())
            .forEach { sbn ->
                NeoChatNotificationStore.onPosted(this, sbn)
            }
    }

    private fun selectController(controllers: List<MediaController>) {
        currentController?.unregisterCallback(controllerCallback)
        clearAlbumArtCache()

        val supportedControllers = controllers.filter {
            NeoSupportedMusicApps.supports(it.packageName)
        }
        currentController = supportedControllers.firstOrNull {
            it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        } ?: supportedControllers.firstOrNull()

        currentController?.registerCallback(controllerCallback)
        publish(currentController)
    }

    private fun clearAlbumArtCache() {
        cachedAlbumArtKey = null
        cachedAlbumArt = null
    }

    private fun albumArtFor(metadata: MediaMetadata?): Bitmap? {
        val sourceKey = listOf(
            metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI),
            metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI),
            metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI),
            metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
            metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
        ).joinToString("|")

        if (sourceKey == cachedAlbumArtKey) {
            return cachedAlbumArt
        }

        val loaded = runCatching {
            metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)?.let { uri ->
                    contentResolver.openInputStream(android.net.Uri.parse(uri))?.use(
                        BitmapFactory::decodeStream
                    )
                }
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)?.let { uri ->
                    contentResolver.openInputStream(android.net.Uri.parse(uri))?.use(
                        BitmapFactory::decodeStream
                    )
                }
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)?.let { uri ->
                    contentResolver.openInputStream(android.net.Uri.parse(uri))?.use(
                        BitmapFactory::decodeStream
                    )
                }
        }.getOrNull()

        val limited = loaded?.let { bitmap ->
            val largestSide = maxOf(bitmap.width, bitmap.height)
            if (largestSide <= 256) {
                bitmap
            } else {
                runCatching {
                    val scale = 256f / largestSide.toFloat()
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt().coerceAtLeast(1),
                        (bitmap.height * scale).toInt().coerceAtLeast(1),
                        true
                    )
                }.getOrDefault(bitmap)
            }
        }

        cachedAlbumArtKey = sourceKey
        cachedAlbumArt = limited
        return limited
    }

    fun controlPlayback(action: String) {
        val controller = currentController
            ?.takeIf { NeoSupportedMusicApps.supports(it.packageName) }
        val controls = controller?.transportControls
        if (controls != null) {
            runCatching {
                when (action) {
                    "previous" -> controls.skipToPrevious()
                    "next" -> controls.skipToNext()
                    "toggle" -> {
                        if (controller.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING) {
                            controls.pause()
                        } else {
                            controls.play()
                        }
                    }
                }
            }
        } else {
            val audioManager = getSystemService(android.media.AudioManager::class.java)
            val keyCode = when (action) {
                "previous" -> android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
                "next" -> android.view.KeyEvent.KEYCODE_MEDIA_NEXT
                else -> android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            }
            audioManager.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, keyCode))
        }
    }

    private fun publish(controller: MediaController?) {
        if (controller == null || !NeoSupportedMusicApps.supports(controller.packageName)) {
            clearAlbumArtCache()
            NeoMusicSessionStore.update(null)
            return
        }

        val appInfo = runCatching {
            packageManager.getApplicationInfo(controller.packageName, 0)
        }.getOrNull() ?: return

        val label = packageManager.getApplicationLabel(appInfo).toString()
        preferences.setLastMusicPackage(controller.packageName)
        val metadata = controller.metadata
        val playbackState = controller.playbackState
        val positionUpdatedAtMs = System.currentTimeMillis()
        val albumArt = albumArtFor(metadata)

        NeoMusicSessionStore.update(
            MusicInfo(
                packageName = controller.packageName,
                appLabel = label,
                title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
                albumArt = albumArt,
                positionMs = (playbackState?.position ?: 0L).coerceAtLeast(0L),
                durationMs = (metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L).coerceAtLeast(0L),
                playbackSpeed = (playbackState?.playbackSpeed ?: 1f).coerceAtLeast(0f),
                positionUpdatedAtMs = positionUpdatedAtMs,
                isPlaying = playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
            )
        )
    }
}
