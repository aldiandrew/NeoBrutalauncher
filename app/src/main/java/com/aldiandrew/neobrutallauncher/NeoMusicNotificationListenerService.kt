package com.aldiandrew.neobrutallauncher

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.graphics.drawable.toBitmap

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
        if (sbn.packageName == currentController?.packageName) publish(currentController)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NeoChatNotificationStore.onRemoved(sbn)
        refreshChatNotifications()
        if (sbn.packageName == currentController?.packageName) publish(currentController)
    }

    fun refreshChatNotifications() {
        NeoChatNotificationStore.clear()
        runCatching { activeNotifications.orEmpty() }
            .getOrDefault(emptyArray())
            .forEach { sbn -> NeoChatNotificationStore.onPosted(this, sbn) }
    }

    private fun selectController(controllers: List<MediaController>) {
        currentController?.unregisterCallback(controllerCallback)
        clearAlbumArtCache()

        // Prefer an actively playing supported player, but do not ignore other media apps.
        // The current player can be YouTube, a browser, or an OEM player not in our legacy list.
        currentController = controllers.firstOrNull {
            NeoSupportedMusicApps.supports(it.packageName) &&
                it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull {
            it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull {
            NeoSupportedMusicApps.supports(it.packageName) &&
                !it.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrBlank()
        } ?: controllers.firstOrNull {
            !it.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).isNullOrBlank()
        } ?: controllers.firstOrNull {
            NeoSupportedMusicApps.supports(it.packageName)
        } ?: controllers.firstOrNull()

        currentController?.registerCallback(controllerCallback)
        publish(currentController)
    }

    private fun clearAlbumArtCache() {
        cachedAlbumArtKey = null
        cachedAlbumArt = null
    }

    @Suppress("DEPRECATION")
    private fun notificationAlbumArt(packageName: String): Bitmap? {
        val notification = runCatching {
            activeNotifications.orEmpty()
                .filter { it.packageName == packageName }
                .maxByOrNull { it.postTime }
                ?.notification
        }.getOrNull() ?: return null
        val extras = notification.extras
        val bitmap = sequenceOf(
            extras.getParcelable<Bitmap>(Notification.EXTRA_LARGE_ICON),
            extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE),
        ).filterNotNull().firstOrNull()
        if (bitmap != null) return bitmap

        return runCatching {
            extras.getParcelable<android.graphics.drawable.Icon>(Notification.EXTRA_LARGE_ICON)
                ?.loadDrawable(this)
                ?.toBitmap(256, 256)
        }.getOrNull()
    }

    private fun albumArtFor(metadata: MediaMetadata?, packageName: String): Bitmap? {
        val notification = runCatching {
            activeNotifications.orEmpty()
                .filter { it.packageName == packageName }
                .maxByOrNull { it.postTime }
        }.getOrNull()
        val sourceKey = listOf(
            metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI),
            metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI),
            metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI),
            metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
            metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM),
            notification?.key,
            notification?.postTime?.toString()
        ).joinToString("|")

        if (sourceKey == cachedAlbumArtKey) return cachedAlbumArt

        val loaded = runCatching {
            metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)?.let { uri ->
                    contentResolver.openInputStream(android.net.Uri.parse(uri))?.use(BitmapFactory::decodeStream)
                }
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)?.let { uri ->
                    contentResolver.openInputStream(android.net.Uri.parse(uri))?.use(BitmapFactory::decodeStream)
                }
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)?.let { uri ->
                    contentResolver.openInputStream(android.net.Uri.parse(uri))?.use(BitmapFactory::decodeStream)
                }
                ?: notificationAlbumArt(packageName)
        }.getOrNull()

        val limited = loaded?.let { bitmap ->
            val largestSide = maxOf(bitmap.width, bitmap.height)
            if (largestSide <= 256) bitmap else runCatching {
                val scale = 256f / largestSide.toFloat()
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            }.getOrDefault(bitmap)
        }

        cachedAlbumArtKey = sourceKey
        cachedAlbumArt = limited
        return limited
    }

    fun controlPlayback(action: String) {
        val controller = currentController
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
        if (controller == null) {
            clearAlbumArtCache()
            NeoMusicSessionStore.update(null)
            return
        }

        val appInfo = runCatching { packageManager.getApplicationInfo(controller.packageName, 0) }.getOrNull()
            ?: return
        val label = packageManager.getApplicationLabel(appInfo).toString()
        preferences.setLastMusicPackage(controller.packageName)
        val metadata = controller.metadata
        val playbackState = controller.playbackState

        NeoMusicSessionStore.update(
            MusicInfo(
                packageName = controller.packageName,
                appLabel = label,
                title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
                albumArt = albumArtFor(metadata, controller.packageName),
                positionMs = (playbackState?.position ?: 0L).coerceAtLeast(0L),
                durationMs = (metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L).coerceAtLeast(0L),
                playbackSpeed = (playbackState?.playbackSpeed ?: 1f).coerceAtLeast(0f),
                positionUpdatedAtMs = System.currentTimeMillis(),
                isPlaying = playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
            )
        )
    }
}
