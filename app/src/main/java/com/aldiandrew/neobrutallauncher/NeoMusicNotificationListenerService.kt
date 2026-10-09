package com.aldiandrew.neobrutallauncher

import android.content.ComponentName
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.session.PlaybackState
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
    private var observedControllers: List<MediaController> = emptyList()
    private val preferences by lazy { LauncherPreferences(applicationContext) }
    private var cachedAlbumArtKey: String? = null
    private var cachedAlbumArt: Bitmap? = null

    private val sessionListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            selectController(controllers ?: emptyList())
        }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            refreshActiveSessions()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            refreshActiveSessions()
        }

        override fun onSessionDestroyed() {
            refreshActiveSessions()
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
        observedControllers.forEach { controller ->
            runCatching { controller.unregisterCallback(controllerCallback) }
        }
        observedControllers = emptyList()
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

    private fun refreshActiveSessions() {
        if (!::sessionManager.isInitialized) return
        val controllers = runCatching {
            sessionManager.getActiveSessions(
                ComponentName(this, NeoMusicNotificationListenerService::class.java)
            )
        }.getOrDefault(emptyList())
        selectController(controllers)
    }

    private fun selectController(controllers: List<MediaController>) {
        observedControllers.forEach { controller ->
            runCatching { controller.unregisterCallback(controllerCallback) }
        }
        observedControllers = controllers.distinctBy { it.sessionToken }
        observedControllers.forEach { controller ->
            runCatching { controller.registerCallback(controllerCallback) }
        }

        val selected = observedControllers
            .mapNotNull { controller ->
                mediaSessionScore(controller)?.let { score -> controller to score }
            }
            .maxByOrNull { it.second }
            ?.first

        if (currentController?.sessionToken != selected?.sessionToken) {
            clearAlbumArtCache()
        }
        currentController = selected
        publish(selected)
    }

    /**
     * Android exposes active media sessions, not a guaranteed "music-only" category.
     * Score the session state and media metadata, while excluding clear call audio.
     * Package names are only a secondary hint, never the sole selection rule.
     */
    private fun mediaSessionScore(controller: MediaController): Int? {
        val state = controller.playbackState?.state ?: return null
        val metadata = controller.metadata
        val attributes = controller.playbackInfo?.audioAttributes
        val usage = attributes?.usage

        if (
            usage == AudioAttributes.USAGE_VOICE_COMMUNICATION ||
            usage == AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING
        ) {
            return null
        }

        val playing = state == PlaybackState.STATE_PLAYING
        val resumable = state == PlaybackState.STATE_PAUSED ||
            state == PlaybackState.STATE_BUFFERING ||
            state == PlaybackState.STATE_CONNECTING
        if (!playing && !resumable) return null

        fun value(key: String): String? =
            metadata?.getString(key)?.trim()?.takeIf { it.isNotEmpty() }

        val title = value(MediaMetadata.METADATA_KEY_TITLE)
            ?: value(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        val artist = value(MediaMetadata.METADATA_KEY_ARTIST)
            ?: value(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
        val album = value(MediaMetadata.METADATA_KEY_ALBUM)
        val description = value(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
        val hasMediaMetadata = title != null || artist != null || album != null || description != null

        val packageName = controller.packageName.lowercase(java.util.Locale.ROOT)
        val communicationPackage = listOf(
            "whatsapp", "telegram", "signal", "discord", "skype",
            "com.facebook.orca", "com.google.android.apps.meet", "com.instagram.android",
            "com.zing.zalo"
        ).any { packageName.contains(it) }

        val metadataText = listOfNotNull(title, artist, album, description)
            .joinToString(" ")
            .lowercase(java.util.Locale.ROOT)
        val callLikeMetadata = listOf(
            "voice call", "video call", "audio call", "incoming call",
            "outgoing call", "calling", "ringing"
        ).any { metadataText.contains(it) }

        if (communicationPackage && callLikeMetadata) return null
        // A communication app with no track-like metadata and no explicit media usage
        // is more likely exposing a call/session shell than a playable media item.
        if (
            communicationPackage &&
            !hasMediaMetadata &&
            usage != AudioAttributes.USAGE_MEDIA
        ) {
            return null
        }

        var score = if (playing) 100 else 30
        if (hasMediaMetadata) score += 25
        if (title != null) score += 15
        if (artist != null) score += 5
        if (album != null) score += 5
        if (usage == AudioAttributes.USAGE_MEDIA) score += 20
        if (communicationPackage) score -= 20
        return score
    }

    private fun clearAlbumArtCache() {
        cachedAlbumArtKey = null
        cachedAlbumArt = null
    }

    private fun albumArtFor(metadata: MediaMetadata?): Bitmap? {
        val sourceKey = listOf(
            metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI),
            metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI),
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

    private fun publish(controller: MediaController?) {
        if (controller == null) {
            clearAlbumArtCache()
            NeoMusicSessionStore.update(null)
            return
        }

        if (mediaSessionScore(controller) == null) {
            NeoMusicSessionStore.update(null)
            return
        }

        val appInfo = runCatching {
            packageManager.getApplicationInfo(controller.packageName, 0)
        }.getOrNull() ?: run {
            NeoMusicSessionStore.update(null)
            return
        }

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
