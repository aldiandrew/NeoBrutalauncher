package com.aldiandrew.neobrutallauncher

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NeoBrutalLauncherApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AppRepository(context) }
    val preferences = remember { LauncherPreferences(context) }

    var apps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var currentPage by remember { mutableStateOf(0) }
    var settingsOpen by remember { mutableStateOf(false) }

    var themePreference by remember { mutableStateOf(preferences.theme()) }
    var themeProfile by remember { mutableStateOf(preferences.themeProfile()) }
    var use24Hour by remember { mutableStateOf(preferences.use24Hour()) }
    var showAmPm by remember { mutableStateOf(preferences.showAmPm()) }
    var homeAppCount by remember { mutableStateOf(preferences.homeAppCount()) }
    var showWeather by remember { mutableStateOf(preferences.showWeather()) }
    var favorites by remember { mutableStateOf(preferences.favorites()) }
    var tilePositions by remember { mutableStateOf(preferences.tilePositions()) }
    var tileSizes by remember { mutableStateOf(preferences.tileSizes()) }
    var appTileContentMode by remember { mutableStateOf(preferences.appTileContentMode()) }
    var typographyStyle by remember { mutableStateOf(preferences.typographyStyle()) }
    var iconPackPackage by remember { mutableStateOf(preferences.iconPackPackage()) }
    var iconThemeStyle by remember { mutableStateOf(preferences.iconThemeStyle()) }
    var wallpaperUri by remember { mutableStateOf(preferences.wallpaperUri()) }
    var customQuotes by remember { mutableStateOf(preferences.customQuotes()) }
    var animationStyle by remember { mutableStateOf(preferences.animationStyle()) }
    var motionSmoothness by remember { mutableStateOf(preferences.motionSmoothness()) }
    var reduceMotion by remember { mutableStateOf(preferences.reduceMotion()) }
    var launchApp by remember { mutableStateOf<AppInfo?>(null) }
    var homeReturnTrigger by remember { mutableIntStateOf(0) }
    var launcherWasPaused by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationAccessGranted by remember {
        mutableStateOf(
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        )
    }

    var selectedChatPackages by remember {
        mutableStateOf(preferences.chatNotificationPackages())
    }

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            wallpaperUri = uri.toString()
            preferences.setWallpaperUri(uri.toString())
        }
    }

    val backupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(preferences.exportBackupJson().toByteArray(Charsets.UTF_8))
                } ?: error("Unable to open backup destination")
            }.onFailure {
                android.widget.Toast.makeText(context, "BACKUP FAILED", android.widget.Toast.LENGTH_SHORT).show()
            }.onSuccess {
                android.widget.Toast.makeText(context, "BACKUP SAVED", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val result = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > 512 * 1024) error("Backup too large")
                        output.write(buffer, 0, read)
                    }
                    preferences.importBackupJson(output.toString(Charsets.UTF_8.name()))
                } ?: false
            }.getOrDefault(false)

            if (result) {
                android.widget.Toast.makeText(context, "BACKUP RESTORED", android.widget.Toast.LENGTH_SHORT).show()
                (context as? Activity)?.recreate()
            } else {
                android.widget.Toast.makeText(context, "INVALID BACKUP", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
        if (granted) {
            showWeather = true
            preferences.setShowWeather(true)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val refreshScope = rememberCoroutineScope()

    fun refreshApps() {
        refreshScope.launch(Dispatchers.Default) {
            val loadedApps = repository.loadApps()
            withContext(Dispatchers.Main.immediate) {
                apps = loadedApps
                if (!preferences.homeAppsInitialized() && favorites.isEmpty()) {
                    val initialFavorites = loadedApps
                        .filter { it.packageName != context.packageName }
                        .take(5)
                        .map { it.packageName + "/" + it.activityName }
                        .toSet()
                    favorites = initialFavorites
                    preferences.setFavorites(initialFavorites)
                    preferences.setHomeAppsInitialized(true)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                launcherWasPaused = true
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                if (launcherWasPaused) {
                    homeReturnTrigger++
                    launcherWasPaused = false
                }
                locationPermissionGranted =
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
                notificationAccessGranted =
                    NotificationManagerCompat.getEnabledListenerPackages(context)
                        .contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val motionConfig = NeoMotionConfig(
        animationStyle = animationStyle,
        smoothness = motionSmoothness,
        reduceMotion = reduceMotion
    )

    fun requestLaunch(app: AppInfo) {
        if (launchApp == null) {
            launchApp = app
        }
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    when {
        settingsOpen -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                themeProfile = themeProfile,
                typographyStyle = typographyStyle,
            ) {
                CompositionLocalProvider(
                    LocalNeoMotionConfig provides motionConfig,
                    LocalIconThemeStyle provides iconThemeStyle
                ) {
                    SettingsScreen(
                    themePreference = themePreference,
                    themeProfile = themeProfile,
                    use24Hour = use24Hour,
                    showAmPm = showAmPm,
                    homeAppCount = homeAppCount,
                    showWeather = showWeather,
                    appTileContentMode = appTileContentMode,
                    typographyStyle = typographyStyle,
                    iconPackPackage = iconPackPackage,
                    iconThemeStyle = iconThemeStyle,
                    customQuotes = customQuotes,
                    animationStyle = animationStyle,
                    motionSmoothness = motionSmoothness,
                    reduceMotion = reduceMotion,
                    wallpaperUri = wallpaperUri,
                    favoritesCount = favorites.size,
                    locationPermissionGranted = locationPermissionGranted,
                    notificationAccessGranted = notificationAccessGranted,
                    chatNotificationPackages = selectedChatPackages,
                    apps = apps,
                    onBack = { settingsOpen = false },
                    onThemeChange = {
                        themePreference = it
                        preferences.setTheme(it)
                    },
                    onThemeProfileChange = {
                        themeProfile = it
                        preferences.setThemeProfile(it)
                    },
                    onUse24HourChange = {
                        use24Hour = it
                        preferences.setUse24Hour(it)
                    },
                    onShowAmPmChange = {
                        showAmPm = it
                        preferences.setShowAmPm(it)
                    },
                    onHomeAppCountChange = { count ->
                        val normalized = when (count) {
                            3, 5, 7 -> count
                            else -> 5
                        }
                        homeAppCount = normalized
                        preferences.setHomeAppCount(normalized)

                        if (favorites.size > normalized) {
                            val favoriteKeysInAppOrder = apps.map {
                                it.packageName + "/" + it.activityName
                            }
                            val trimmed = favoriteKeysInAppOrder
                                .filter { favorites.contains(it) }
                                .take(normalized)
                                .toSet()
                            favorites = trimmed
                            preferences.setFavorites(trimmed)
                        }
                    },
                    onShowWeatherChange = { enabled ->
                        if (enabled) {
                            if (locationPermissionGranted) {
                                showWeather = true
                                preferences.setShowWeather(true)
                            } else {
                                locationPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            }
                        } else {
                            showWeather = false
                            preferences.setShowWeather(false)
                        }
                    },
                    onRequestWeatherPermission = {
                        locationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    },
                    onOpenNotificationAccess = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    onChatNotificationPackagesChange = { updated ->
                        val normalized = updated.distinct().take(1)
                        selectedChatPackages = normalized
                        preferences.setChatNotificationPackages(normalized)
                        NeoChatNotificationStore.setSelectedPackages(normalized)
                        NeoNotificationServiceRegistry.service?.refreshChatNotifications()
                    },
                    onAppTileContentModeChange = {
                        appTileContentMode = it
                        preferences.setAppTileContentMode(it)
                    },
                    onTypographyStyleChange = {
                        typographyStyle = it
                        preferences.setTypographyStyle(it)
                    },
                    onIconPackChange = {
                        iconPackPackage = it
                        preferences.setIconPackPackage(it)
                        refreshApps()
                    },
                    onIconThemeStyleChange = {
                        iconThemeStyle = it
                        preferences.setIconThemeStyle(it)
                    },
                    onCustomQuotesChange = {
                        customQuotes = it
                        preferences.setCustomQuotes(it)
                    },
                    onAnimationStyleChange = {
                        animationStyle = it
                        preferences.setAnimationStyle(it)
                    },
                    onMotionSmoothnessChange = {
                        motionSmoothness = it
                        preferences.setMotionSmoothness(it)
                    },
                    onReduceMotionChange = {
                        reduceMotion = it
                        preferences.setReduceMotion(it)
                    },
                    onBackup = { backupFileLauncher.launch("neo-brutal-launcher-backup.json") },
                    onRestore = { restoreFileLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) },
                    onResetAll = {
                        preferences.resetCustomizations()
                        (context as? Activity)?.recreate()
                    },
                    onChooseWallpaper = {
                        wallpaperPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onClearWallpaper = {
                        wallpaperUri = null
                        preferences.setWallpaperUri(null)
                    },
                    onClearFavorites = {
                        favorites = emptySet()
                        preferences.clearFavorites()
                    }
                    )
                }
            }
        }

        else -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                themeProfile = themeProfile,
                typographyStyle = typographyStyle,
            ) {
                CompositionLocalProvider(
                    LocalNeoMotionConfig provides motionConfig,
                    LocalIconThemeStyle provides iconThemeStyle
                ) {
                    LauncherPageHost(
                        currentPage = currentPage,
                        homeReturnTrigger = homeReturnTrigger,
                        motionConfig = motionConfig,
                    onPageChange = {
                        currentPage = it.coerceIn(0, 2)
                        if (currentPage == 1) refreshApps()
                    }
                ) { page ->
                    if (page == 0) {
                        HomeScreen(
                            apps = apps,
                            favorites = favorites,
                            homeAppCount = homeAppCount,
                            use24Hour = use24Hour,
                            showAmPm = showAmPm,
                            showWeather = showWeather,
                            customQuotes = customQuotes,
                            appTileContentMode = appTileContentMode,
                            typographyStyle = typographyStyle,
                            wallpaperUri = wallpaperUri,
                            onOpenSettings = { settingsOpen = true },
                            onOpenApps = { currentPage = 1 },
                            onLaunch = ::requestLaunch,
                            onHomeAppCountChange = { updated ->
                                homeAppCount = updated
                                preferences.setHomeAppCount(updated)
                            },
                            tilePositions = tilePositions,
                            onTilePositionsChange = { updated ->
                                tilePositions = updated
                                preferences.setTilePositions(updated)
                            },
                            tileSizes = tileSizes,
                            onTileSizeChange = { tileId, size ->
                                tileSizes = tileSizes + (tileId to size)
                                preferences.setTileSizes(tileSizes)
                            },
                        )
                    } else if (page == 1) {
                        AppsPage(
                            apps = apps,
                            favorites = favorites,
                            favoriteLimit = homeAppCount,
                            onToggleFavorite = { app ->
                                val key = app.packageName + "/" + app.activityName
                                val updated = favorites.toMutableSet()
                                if (updated.contains(key)) {
                                    updated.remove(key)
                                } else if (updated.size < homeAppCount) {
                                    updated.add(key)
                                }
                                favorites = updated
                                preferences.setFavorites(updated)
                            },
                            onLaunch = ::requestLaunch,
                            onOpenHome = { currentPage = 0 }
                        )
                    } else {
                        LivePage(
                            apps = apps,
                            customQuotes = customQuotes,
                            selectedChatPackages = selectedChatPackages,
                            onSelectChatPackage = { packageName ->
                                val normalized = listOfNotNull(packageName).take(1)
                                selectedChatPackages = normalized
                                preferences.setChatNotificationPackages(normalized)
                                NeoChatNotificationStore.setSelectedPackages(normalized)
                                NeoNotificationServiceRegistry.service?.refreshChatNotifications()
                            },
                            onOpenHome = { currentPage = 0 }
                        )
                    }
                    }
                    if (launchApp != null) {
                        NeoLaunchTransition(
                            app = launchApp!!,
                            config = motionConfig,
                            onFinished = {
                                val app = launchApp
                                launchApp = null
                                if (app != null) repository.launch(app)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherPageHost(
    currentPage: Int,
    homeReturnTrigger: Int,
    motionConfig: NeoMotionConfig,
    onPageChange: (Int) -> Unit,
    content: @Composable (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { 3 }
    )

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(
                page = currentPage,
                animationSpec = if (motionConfig.reduceMotion) {
                    androidx.compose.animation.core.tween(durationMillis = 1)
                } else {
                    motionConfig.springSpec()
                }
            )
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != currentPage) {
            onPageChange(pagerState.settledPage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
            key = { it }
        ) { page ->
            NeoHomeReturnMotion(
                trigger = homeReturnTrigger,
                config = motionConfig,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content(page)
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 10.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                        .background(
                            if (pagerState.currentPage == index) LocalNeoThemePalette.current.accent(
                                pagerState.currentPage == 2
                            ) else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = LocalNeoThemePalette.current.accent(pagerState.currentPage == 2)
                        )
                )
            }
            Text(
                text = when (pagerState.currentPage) {
                    0 -> "HOME"
                    1 -> "APPS"
                    else -> "LIVE"
                },
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = LocalNeoThemePalette.current.accent(pagerState.currentPage == 2)
            )
        }
    }
}

@Composable
private fun HomeScreen(
    apps: List<AppInfo>,
    favorites: Set<String>,
    homeAppCount: Int,
    use24Hour: Boolean,
    showAmPm: Boolean,
    showWeather: Boolean,
    appTileContentMode: TileContentMode,
    typographyStyle: TypographyStyle,
    wallpaperUri: String?,
    customQuotes: List<String>,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    tilePositions: Map<String, NeoTilePosition>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    tileSizes: Map<String, NeoTileSize>,
    onTileSizeChange: (String, NeoTileSize) -> Unit,
) {
    BackHandler(onBack = {})

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { LauncherPreferences(context) }
    val now = rememberMinuteClock()
    val isDarkTheme = LocalNeoThemeIsDark.current
    val themePalette = LocalNeoThemePalette.current
    val homeClockBackground = themePalette.accent(isDarkTheme)
    val homeClockText = themePalette.onAccent(isDarkTheme)
    val homeMusicBackground = themePalette.secondary(isDarkTheme)
    val homeNotesBackground = themePalette.tilePalette(isDarkTheme).getOrElse(1) { themePalette.surface(isDarkTheme) }
    val quoteRotation = rememberLiveTileData(
        tileId = "home-quotes",
        refreshIntervalMillis = 30L * 60L * 1000L,
        initialValue = (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    ) {
        (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    }.value ?: 0

    var weatherRefreshToken by remember { mutableIntStateOf(0) }
    var noteItems by remember { mutableStateOf(preferences.noteItems()) }
    var selectedTile by remember { mutableStateOf<NeoTileSpec?>(null) }
    var tileEditMode by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var excludedHomeApps by remember { mutableStateOf(preferences.excludedHomeApps()) }
    var appShortcutKey by remember { mutableStateOf(preferences.appShortcutKey()) }

    val timePattern = when {
        use24Hour -> "HH:mm"
        showAmPm -> "hh:mm a"
        else -> "hh:mm"
    }
    val time = remember(timePattern) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val longDay = remember { SimpleDateFormat("EEEE", Locale.ENGLISH) }
    val longDate = remember { SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH) }

    val launchCounts = remember(apps) { preferences.appLaunchCounts() }
    val rankedApps = apps.sortedWith(
        compareByDescending<AppInfo> { launchCounts[it.packageName + "/" + it.activityName] ?: 0 }
            .thenByDescending { favorites.contains(it.packageName + "/" + it.activityName) }
            .thenBy { it.label.lowercase() }
    )

    val appKeys = apps.map { it.packageName + "/" + it.activityName }
    val storedHomeOrder = remember(appKeys) { preferences.homeAppOrder() }
    var stableHomeOrder by remember(appKeys, storedHomeOrder) {
        mutableStateOf(
            run {
                val available = appKeys.toSet()
                val existing = storedHomeOrder.filter { it in available }
                val missing = appKeys.filterNot { it in existing }
                if (existing.isNotEmpty()) {
                    existing + missing
                } else {
                    rankedApps.map { it.packageName + "/" + it.activityName }
                }
            }
        )
    }

    LaunchedEffect(stableHomeOrder) {
        preferences.setHomeAppOrder(stableHomeOrder)
    }

    // A pinned app is a Home launchable app, so pinning also clears a prior removal.
    LaunchedEffect(favorites, excludedHomeApps) {
        val cleaned = excludedHomeApps - favorites
        if (cleaned != excludedHomeApps) {
            excludedHomeApps = cleaned
            preferences.setExcludedHomeApps(cleaned)
        }
    }

    val appsByKey = remember(apps) {
        apps.associateBy { it.packageName + "/" + it.activityName }
    }
    val launchableApps = stableHomeOrder
        .filterNot { excludedHomeApps.contains(it) }
        .sortedWith(
            compareByDescending<String> { favorites.contains(it) }
        )
        .mapNotNull { appsByKey[it] }
        .take(homeAppCount.coerceIn(2, 8))
        .sortedWith(
            compareBy<AppInfo> {
                tilePositions["app_" + it.packageName + "_" + it.activityName]?.row
                    ?: Int.MAX_VALUE
            }
        )

    val shortcutApp = remember(appShortcutKey, appsByKey) { appShortcutKey?.let { appsByKey[it] } }
    val palette = themePalette.tilePalette(isDarkTheme)
    val appTileIds = remember(launchableApps) {
        launchableApps.map { "app_" + it.packageName + "_" + it.activityName }.toSet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (wallpaperUri == null) MaterialTheme.colorScheme.background else Color.Transparent)
    ) {
        BrutalWallpaper(uriString = wallpaperUri, modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 10.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 72.dp,
                    start = 18.dp,
                    end = 18.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "top-bar") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrutalBlock(
                        modifier = Modifier.width(150.dp),
                        background = BrutalColors.Cyan,
                        borderWidth = 3.dp,
                        shadowX = 4.dp,
                        shadowY = 4.dp
                    ) {
                        Text(
                            text = "// Home",
                            fontFamily = BrutalTypography.Display,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = BrutalColors.Ink
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(106.dp)
                            .clickable(onClick = onOpenSettings)
                    ) {
                        BrutalBlock(
                            modifier = Modifier.fillMaxWidth(),
                            background = BrutalColors.Pink,
                            borderWidth = 3.dp,
                            shadowX = 4.dp,
                            shadowY = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = BrutalColors.Ink,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "SETTINGS",
                                    fontFamily = BrutalTypography.Display,
                                    modifier = Modifier,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = BrutalColors.Ink
                                )
                            }
                        }
                    }
                }
            }

            item(key = "clock") {
                BrutalBlock(
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                    background = homeClockBackground,
                    borderWidth = 4.dp,
                    borderColor = BrutalColors.Ink,
                    shadowX = 7.dp,
                    shadowY = 7.dp
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize().padding(13.dp)
                    ) {
                        val compact = minOf(maxWidth, maxHeight)
                        val timeSize = when {
                            compact < 130.dp -> if (use24Hour) 41.sp else 38.sp
                            compact < 160.dp -> if (use24Hour) 49.sp else 46.sp
                            else -> if (use24Hour) 59.sp else 56.sp
                        }
                        val sideSize = when {
                            compact < 130.dp -> 10.sp
                            compact < 160.dp -> 12.sp
                            else -> 15.sp
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            BrutalLabel(
                                text = (if (use24Hour) "24H" else "12H") + " / " + typographyStyle.label,
                                background = if (isDarkTheme) BrutalColors.Cyan else BrutalColors.Pink
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = time.format(now),
                                    modifier = Modifier.weight(1f),
                                    fontSize = timeSize,
                                    lineHeight = timeSize,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = when (typographyStyle) {
                                        TypographyStyle.MONO -> BrutalTypography.Mono
                                        else -> BrutalTypography.Poster
                                    },
                                    color = homeClockText,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip
                                )

                                    Column(
                                        modifier = Modifier
                                            .width(if (compact < 150.dp) 78.dp else 102.dp)
                                            .padding(start = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = longDay.format(now).uppercase(Locale.ENGLISH),
                                            fontSize = sideSize,
                                            lineHeight = sideSize,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = BrutalTypography.Display,
                                            color = BrutalColors.Red,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(homeClockText)
                                        )
                                        Text(
                                            text = longDate.format(now).uppercase(Locale.ENGLISH),
                                            fontSize = sideSize,
                                            lineHeight = sideSize,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = BrutalTypography.Display,
                                            color = homeClockText,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                            }
                        }
                    }
                }
            }

            item(key = "status-row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FixedSmallTile(
                        modifier = Modifier.weight(1f),
                        background = BrutalColors.Purple
                    ) {
                        BrutalTape(
                            text = "MANIFESTO",
                            background = BrutalColors.Yellow
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "YOUR PHONE\nDOESN'T NEED\nTO LOOK CALM.",
                            fontFamily = BrutalTypography.Display,
                            fontSize = 7.5.sp,
                            lineHeight = 8.5.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.25.sp,
                            color = BrutalColors.White,
                            maxLines = 3
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).clickable { weatherRefreshToken++ }
                    ) {
                        NeoWeatherTile(
                            context = context,
                            refreshToken = weatherRefreshToken,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    BatteryTile(
                        context = context,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        background = BrutalColors.Orange
                    )

                    NeoNetworkTile(
                        context = context,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                }
            }

            item(key = "music") {
                NeoMusicTile(
                    context = context,
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f),
                    background = homeMusicBackground,
                    textColor = if (isDarkTheme) BrutalColors.DarkWhite else BrutalColors.Ink
                )
            }

            item(key = "launchable-apps") {
                if (launchableApps.isNotEmpty()) {
                    NeoTileGrid(
                        tiles = buildList {
                            launchableApps.forEachIndexed { index, app ->
                                val id = "app_" + app.packageName + "_" + app.activityName
                                val defaultSize = when {
                                    index == launchableApps.lastIndex -> NeoTileSize.FOUR_BY_ONE
                                    index < 2 -> NeoTileSize.HORIZONTAL
                                    else -> NeoTileSize.SMALL
                                }
                                val size = if (index == launchableApps.lastIndex) {
                                    NeoTileSize.FOUR_BY_ONE
                                } else {
                                    tileSizes[id] ?: defaultSize
                                }
                                val tileColor = palette[index % palette.size]

                                add(
                                    NeoTileSpec(
                                        id = id,
                                        size = size,
                                        label = app.label.uppercase(),
                                        onClick = { onLaunch(app) }
                                    ) {
                                        AppTile(
                                            app = app,
                                            background = tileColor,
                                            modifier = Modifier.fillMaxSize(),
                                            contentMode = appTileContentMode,
                                            tileSize = size,
                                            variant = index
                                        )
                                    }
                                )
                            }
                        },
                        positions = tilePositions.filterKeys { appTileIds.contains(it) },
                        onPositionsChange = onTilePositionsChange,
                        onTileLongPress = { tile ->
                            selectedTile = tile
                        },
                        onTileEdit = { selectedTile = it },
                        onTileMoveFinished = { tileEditMode = false },
                        editMode = tileEditMode,
                        modifier = Modifier.fillMaxWidth(),
                        gap = 8.dp
                    )
                }
            }

            item(key = "notes-tasks") {
                NeoNotesTasksTile(
                    notes = noteItems,
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                    background = homeNotesBackground,
                    textColor = if (isDarkTheme) BrutalColors.DarkWhite else BrutalColors.Ink,
                    onAddNote = { text ->
                        val updated = noteItems + NeoListItem(text = text)
                        noteItems = updated
                        preferences.setNoteItems(updated)
                    },
                    onEditNote = { index, text ->
                        val updated = noteItems.mapIndexed { itemIndex, item ->
                            if (itemIndex == index) item.copy(text = text) else item
                        }
                        noteItems = updated
                        preferences.setNoteItems(updated)
                    }
                )
            }

            item(key = "footer") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoAddAppTile(
                        app = shortcutApp,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        onClick = {
                            shortcutApp?.let(onLaunch) ?: run { showAppPicker = true }
                        },
                        onLongClick = { showAppPicker = true }
                    )
                    val quotePair = NeoQuotes.pairForRotation(quoteRotation, customQuotes)
                    NeoQuoteTilePlain(
                        quote = quotePair.first,
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        paletteIndex = quoteRotation
                    )
                    NeoQuoteTilePlain(
                        quote = quotePair.second,
                        modifier = Modifier.weight(2f).aspectRatio(2f),
                        paletteIndex = quoteRotation + 1
                    )
                }
            }
        }
    }

    if (showAppPicker) {
        val sortedPickerApps = apps.sortedBy { it.label.lowercase() }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = {
                Text(
                    text = "APP SHORTCUT",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SELECT THE APP USED BY THE + APP SHORTCUT TILE.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )

                    if (sortedPickerApps.isEmpty()) {
                        Text(
                            text = "NO LAUNCHABLE APPS",
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(420.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(
                                sortedPickerApps,
                                key = { it.packageName + "/" + it.activityName }
                            ) { app ->
                                val key = app.packageName + "/" + app.activityName
                                val selected = key == appShortcutKey

                                BrutalBlock(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            appShortcutKey = key
                                            preferences.setAppShortcutKey(key)
                                            showAppPicker = false
                                        },
                                    background = if (selected) {
                                        BrutalColors.Yellow
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    },
                                    borderWidth = 2.dp,
                                    shadowX = if (selected) 0.dp else 3.dp,
                                    shadowY = if (selected) 0.dp else 3.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = app.label.uppercase(),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (selected) {
                                                BrutalColors.Ink
                                            } else {
                                                MaterialTheme.colorScheme.onBackground
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (selected) "SELECTED" else "USE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.Orange
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (appShortcutKey != null) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                appShortcutKey = null
                                preferences.setAppShortcutKey(null)
                                showAppPicker = false
                            }
                        ) {
                            Text("CLEAR SHORTCUT", fontWeight = FontWeight.Black)
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAppPicker = false }) {
                    Text("CLOSE", fontWeight = FontWeight.Black)
                }
            }
        )
    }

    selectedTile?.let { tile ->
        val lastId = launchableApps.lastOrNull()?.let {
            "app_" + it.packageName + "_" + it.activityName
        }
        val locked = tile.id == lastId

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                selectedTile = null
                tileEditMode = false
            },
            title = {
                Text(
                    text = "EDIT TILE",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (locked) {
                            "FIXED 4x1 / LAST HOME APP"
                        } else {
                            "CURRENT SIZE: " + tile.size.label
                        },
                        fontWeight = FontWeight.Black
                    )
                    if (!locked) {
                        listOf(
                            NeoTileSize.SMALL,
                            NeoTileSize.HORIZONTAL,
                            NeoTileSize.THREE_BY_ONE,
                            NeoTileSize.FOUR_BY_ONE
                        ).forEach { option ->
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    onTileSizeChange(tile.id, option)
                                    selectedTile = null
                                    tileEditMode = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "SET " + option.label,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!locked && homeAppCount > 2) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                val app = launchableApps.firstOrNull {
                                    "app_" + it.packageName + "_" + it.activityName == tile.id
                                }
                                if (app != null) {
                                    val key = app.packageName + "/" + app.activityName
                                    val updatedExcluded = excludedHomeApps + key
                                    excludedHomeApps = updatedExcluded
                                    preferences.setExcludedHomeApps(updatedExcluded)
                                    onHomeAppCountChange((homeAppCount - 1).coerceAtLeast(2))
                                }
                                selectedTile = null
                                tileEditMode = false
                            }
                        ) {
                            Text(
                                text = "REMOVE FROM HOME",
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Orange
                            )
                        }
                    }

                    androidx.compose.material3.TextButton(
                        onClick = {
                            selectedTile = null
                            tileEditMode = true
                        }
                    ) {
                        Text(
                            text = "MOVE TILE",
                            fontWeight = FontWeight.Black
                        )
                    }

                    androidx.compose.material3.TextButton(
                        onClick = {
                            selectedTile = null
                            tileEditMode = false
                        }
                    ) {
                        Text(text = "DONE", fontWeight = FontWeight.Black)
                    }
                }
            }
        )
    }
}

@Composable
private fun FixedSmallTile(
    modifier: Modifier,
    background: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    BrutalBlock(
        modifier = modifier.aspectRatio(1f),
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp,
        content = content
    )
}

@Composable
private fun NeoAddAppTile(
    app: AppInfo?,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconThemeStyle = LocalIconThemeStyle.current

    Box(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = MaterialTheme.colorScheme.surface,
            borderWidth = 3.dp,
            borderColor = textColor,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            if (app == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+ APP",
                        fontSize = 16.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                    Text(
                        text = "SET SHORTCUT",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NeoAppIcon(
                        app = app,
                        size = 48.dp,
                        style = iconThemeStyle
                    )
                    if (iconThemeStyle.showsIcon()) {
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(
                        text = app.label.uppercase(),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = BrutalTypography.Display,
                        fontSize = when {
                            app.label.length > 18 -> 8.sp
                            app.label.length > 11 -> 9.sp
                            else -> 11.sp
                        },
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NeoQuoteTilePlain(
    quote: String,
    modifier: Modifier,
    paletteIndex: Int
) {
    val palette = NeoQuotes.paletteForRotation(paletteIndex)

    BrutalBlock(
        modifier = modifier,
        background = palette.background,
        borderWidth = 4.dp,
        borderColor = BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = BrutalColors.Ink
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 9.dp, top = 9.dp, end = 9.dp, bottom = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "“" + quote + "”",
                    fontFamily = BrutalTypography.Body,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = palette.text,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
    }
}

@Composable
fun NeoQuoteTile(
    quote: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    paletteIndex: Int = 0
) {
    val palette = NeoQuotes.paletteForRotation(paletteIndex)

    BrutalBlock(
        modifier = modifier,
        background = palette.background,
        borderWidth = 4.dp,
        borderColor = BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = BrutalColors.Ink
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 9.dp, top = 9.dp, end = 9.dp, bottom = 10.dp)
        ) {
                val compact = minOf(maxWidth, maxHeight)
                val availableHeight = (maxHeight.value - if (emphasized) 43f else 30f)
                    .coerceAtLeast(18f)
                var quoteSize = if (emphasized) {
                    minOf(34f, maxWidth.value * 0.11f, availableHeight * 0.34f)
                } else {
                    when {
                        compact < 78.dp -> 7f
                        compact < 155.dp -> 12f
                        else -> 17f
                    }
                }.coerceAtLeast(7f)

                while (quoteSize > 9f) {
                    val estimatedCharsPerLine =
                        (maxWidth.value / (quoteSize * 0.58f)).toInt().coerceAtLeast(8)
                    val estimatedLines =
                        ((quote.length + estimatedCharsPerLine - 1) / estimatedCharsPerLine)
                            .coerceAtLeast(1)
                    val neededHeight = estimatedLines * quoteSize * 1.08f
                    if (neededHeight <= availableHeight) break
                    quoteSize -= 1f
                }

                val charsPerLine =
                    (maxWidth.value / (quoteSize * 0.58f)).toInt().coerceAtLeast(8)
                val estimatedLines =
                    ((quote.length + charsPerLine - 1) / charsPerLine)
                        .coerceAtLeast(1)
                        .coerceAtMost(6)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(if (emphasized) 6.dp else 5.dp)
                ) {
                    Text(
                        text = "“$quote”",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = BrutalTypography.Body,
                        fontSize = quoteSize.sp,
                        lineHeight = (quoteSize * 1.08f).sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.text,
                        maxLines = estimatedLines,
                        softWrap = true,
                        overflow = TextOverflow.Clip
                    )
                }
            }
    }
}

@Composable
private fun AppTile(
    app: AppInfo,
    background: Color,
    modifier: Modifier,
    contentMode: TileContentMode,
    tileSize: NeoTileSize,
    variant: Int
) {
    BoxWithConstraints(modifier = modifier) {
        val iconThemeStyle = LocalIconThemeStyle.current
        val effectiveContentMode =
            if (iconThemeStyle == IconThemeStyle.TEXT_ONLY) TileContentMode.TEXT else contentMode

        val iconSize = when (tileSize) {
            NeoTileSize.SMALL -> 28.dp
            NeoTileSize.HORIZONTAL -> 30.dp
            NeoTileSize.THREE_BY_ONE -> 30.dp
            NeoTileSize.FOUR_BY_ONE -> minOf(maxHeight * 0.78f, 78.dp)
        }

        val appTileTextColor = if (LocalNeoThemeIsDark.current) BrutalColors.DarkWhite else BrutalColors.Ink

        val maxTextSize = when (tileSize) {
            NeoTileSize.SMALL -> 9.sp
            NeoTileSize.HORIZONTAL -> 15.sp
            NeoTileSize.THREE_BY_ONE -> 23.sp
            NeoTileSize.FOUR_BY_ONE -> 30.sp
        }

        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = background,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            Box(Modifier.fillMaxSize()) {
                when (effectiveContentMode) {
                    TileContentMode.ICON -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            NeoAppIcon(
                                app = app,
                                size = iconSize,
                                style = iconThemeStyle
                            )
                        }
                    }

                    TileContentMode.TEXT -> {
                        val isFourByOne = tileSize == NeoTileSize.FOUR_BY_ONE
                        val isSmall = tileSize == NeoTileSize.SMALL
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = when {
                                isFourByOne -> Alignment.CenterEnd
                                isSmall -> Alignment.Center
                                else -> Alignment.CenterStart
                            }
                        ) {
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = when {
                                    isFourByOne -> TextAlign.End
                                    isSmall -> TextAlign.Center
                                    else -> TextAlign.Start
                                },
                                fontFamily = BrutalTypography.Display,
                                fontSize = maxTextSize,
                                lineHeight = (maxTextSize.value * 1.02f).sp,
                                fontWeight = FontWeight.Black,
                                color = appTileTextColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    TileContentMode.ICON_TEXT -> {
                        if (tileSize == NeoTileSize.SMALL) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                NeoAppIcon(
                                    app = app,
                                    size = iconSize,
                                    style = iconThemeStyle
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = app.label.uppercase(),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontFamily = BrutalTypography.Display,
                                    fontSize = maxTextSize,
                                    lineHeight = (maxTextSize.value * 1.02f).sp,
                                    fontWeight = FontWeight.Black,
                                    color = appTileTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeoAppIcon(
                                    app = app,
                                    size = iconSize,
                                    style = iconThemeStyle
                                )
                                Text(
                                    text = app.label.uppercase(),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start,
                                    fontFamily = BrutalTypography.Display,
                                    fontSize = maxTextSize,
                                    lineHeight = (maxTextSize.value * 1.02f).sp,
                                    fontWeight = FontWeight.Black,
                                    color = appTileTextColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    apps: List<AppInfo>,
    chatNotificationPackages: List<String>,
    themePreference: ThemePreference,
    themeProfile: NeoThemeProfile,
    use24Hour: Boolean,
    showAmPm: Boolean,
    homeAppCount: Int,
    showWeather: Boolean,
    appTileContentMode: TileContentMode,
    typographyStyle: TypographyStyle,
    iconPackPackage: String?,
    iconThemeStyle: IconThemeStyle,
    animationStyle: AnimationStyle,
    motionSmoothness: MotionSmoothness,
    reduceMotion: Boolean,
    wallpaperUri: String?,
    customQuotes: List<String>,
    favoritesCount: Int,
    locationPermissionGranted: Boolean,
    notificationAccessGranted: Boolean,
    onChatNotificationPackagesChange: (List<String>) -> Unit,
    onBack: () -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onThemeProfileChange: (NeoThemeProfile) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onShowAmPmChange: (Boolean) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    onShowWeatherChange: (Boolean) -> Unit,
    onRequestWeatherPermission: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onAppTileContentModeChange: (TileContentMode) -> Unit,
    onTypographyStyleChange: (TypographyStyle) -> Unit,
    onIconPackChange: (String?) -> Unit,
    onIconThemeStyleChange: (IconThemeStyle) -> Unit,
    onCustomQuotesChange: (List<String>) -> Unit,
    onAnimationStyleChange: (AnimationStyle) -> Unit,
    onMotionSmoothnessChange: (MotionSmoothness) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onChooseWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    onClearFavorites: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onResetAll: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiBackground = MaterialTheme.colorScheme.background
    val uiSurface = MaterialTheme.colorScheme.surface
    val uiOnSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalNeoThemeIsDark.current
    val themePalette = LocalNeoThemePalette.current
    val darkTileBackground = if (isDark) BrutalColors.DarkTile else BrutalColors.Ink
    var editedQuotes by remember(customQuotes) { mutableStateOf(customQuotes) }
    val iconPacks = remember { IconPackManager(context).installedIconPacks() }
    val chatCandidates = remember(apps) {
        apps.groupBy { it.packageName }.values.mapNotNull { it.firstOrNull() }
            .filter { it.packageName != context.packageName }.sortedBy { it.label.lowercase() }
    }
    var showChatAppPicker by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    val selectedChatApp = chatCandidates.firstOrNull { it.packageName == chatNotificationPackages.firstOrNull() }

    Column(
        modifier = Modifier.fillMaxSize().background(uiBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = uiOnSurface) }
            BrutalBlock(modifier = Modifier.weight(1f), background = themePalette.accent(isDark), borderWidth = 4.dp, shadowX = 6.dp, shadowY = 6.dp) {
                Text("SETTINGS", fontFamily = BrutalTypography.Display, fontSize = 26.sp, fontWeight = FontWeight.Normal, color = themePalette.onAccent(isDark))
            }
        }

        SettingsSectionTitle("THEME")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("APPEARANCE", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal, color = uiOnSurface)
                Text("Choose the launcher light/dark behavior.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeButton("SYSTEM", themePreference == ThemePreference.SYSTEM, BrutalColors.Cyan, Modifier.weight(1f)) { onThemeChange(ThemePreference.SYSTEM) }
                    ThemeButton("LIGHT", themePreference == ThemePreference.LIGHT, BrutalColors.Yellow, Modifier.weight(1f)) { onThemeChange(ThemePreference.LIGHT) }
                    ThemeButton("DARK", themePreference == ThemePreference.DARK, BrutalColors.Pink, Modifier.weight(1f)) { onThemeChange(ThemePreference.DARK) }
                }
                Text("COLOR THEME", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal, color = uiOnSurface)
                Text("Choose the launcher color identity independently from light/dark mode.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    NeoThemeProfile.values().toList().chunked(3).forEach { rowProfiles ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowProfiles.forEach { profile ->
                                val profilePalette = NeoThemePalettes.forProfile(profile)
                                ThemeButton(
                                    label = profile.label,
                                    selected = themeProfile == profile,
                                    background = profilePalette.lightAccent,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onThemeProfileChange(profile) }
                                )
                            }
                            repeat(3 - rowProfiles.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        SettingsSectionTitle("CLOCK")
        SettingsSwitch("24-HOUR TIME", "Use 24-hour time on Home.", use24Hour, BrutalColors.Yellow, onUse24HourChange)
        if (!use24Hour) SettingsSwitch("AM / PM", "Show the AM/PM marker with 12-hour time.", showAmPm, BrutalColors.Cyan, onShowAmPmChange)
        BrutalBlock(Modifier.fillMaxWidth(), background = BrutalColors.Lime, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TYPOGRAPHY", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TypographyStyle.values().forEach { style -> ThemeButton(style.label, typographyStyle == style, BrutalColors.Cyan, Modifier.weight(1f)) { onTypographyStyleChange(style) } }
                }
            }
        }

        SettingsSectionTitle("MOTION")
        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = BrutalColors.Yellow,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ANIMATION STYLE", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal)
                Text(
                    "Controls page motion and app launch motion. Smooth is the restrained default.",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AnimationStyle.values().forEach { style ->
                        ThemeButton(
                            label = style.label,
                            selected = animationStyle == style,
                            background = when (style) {
                                AnimationStyle.SMOOTH -> BrutalColors.Cyan
                                AnimationStyle.TAP_FLIP -> BrutalColors.Pink
                                AnimationStyle.CUBE_3D -> BrutalColors.Purple
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { onAnimationStyleChange(style) }
                        )
                    }
                }
                Text("SMOOTHNESS", fontFamily = BrutalTypography.Display, fontSize = 15.sp, fontWeight = FontWeight.Normal)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MotionSmoothness.values().forEach { smoothness ->
                        ThemeButton(
                            label = smoothness.label,
                            selected = motionSmoothness == smoothness,
                            background = BrutalColors.Lime,
                            modifier = Modifier.weight(1f),
                            onClick = { onMotionSmoothnessChange(smoothness) }
                        )
                    }
                }
            }
        }
        SettingsSwitch(
            "REDUCE MOTION",
            "Disable decorative launch and return movement while keeping normal launcher behavior.",
            reduceMotion,
            BrutalColors.Cyan,
            onReduceMotionChange
        )

        SettingsSectionTitle("HOME CONTENT")
        SettingsSwitch("WEATHER", "Show local weather. Location permission is required.", showWeather, BrutalColors.Lime, onShowWeatherChange)
        if (!locationPermissionGranted) BrutalActionButton("ALLOW WEATHER LOCATION", BrutalColors.Orange, onClick = onRequestWeatherPermission)

        SettingsSectionTitle("QUOTES")
        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "QUOTES EDITOR",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = uiOnSurface
                )
                Text(
                    "${editedQuotes.size} CUSTOM / ${NeoQuotes.allQuotes(customQuotes).size} TOTAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = uiOnSurface.copy(alpha = .7f)
                )
                if (editedQuotes.isEmpty()) {
                    Text(
                        "NO CUSTOM QUOTES. BUILT-IN QUOTES ARE STILL USED.",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = uiOnSurface.copy(alpha = .7f)
                    )
                } else {
                    editedQuotes.forEachIndexed { index, quote ->
                        val quotePalette = themePalette.tilePalette(isDark)
                        BrutalBlock(
                            Modifier.fillMaxWidth(),
                            background = quotePalette[index % quotePalette.size],
                            borderWidth = 2.dp,
                            shadowX = 3.dp,
                            shadowY = 3.dp
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = (index + 1).toString() + ".",
                                    modifier = Modifier.padding(start = 7.dp, top = 10.dp),
                                    fontFamily = BrutalTypography.Display,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink
                                )
                                BasicTextField(
                                    value = quote,
                                    onValueChange = { value ->
                                        editedQuotes = editedQuotes.toMutableList().also {
                                            it[index] = value.take(300)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 7.dp, vertical = 8.dp),
                                    minLines = 2,
                                    maxLines = 4,
                                    textStyle = TextStyle(
                                        color = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (quote.isBlank()) {
                                            Text(
                                                "WRITE A QUOTE…",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isDark) BrutalColors.DarkWhite.copy(alpha = .45f) else BrutalColors.Ink.copy(alpha = .45f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                ThemeButton(
                                    label = "DELETE",
                                    selected = true,
                                    background = BrutalColors.Pink,
                                    modifier = Modifier
                                        .width(66.dp)
                                        .padding(end = 5.dp, top = 6.dp),
                                    onClick = {
                                        editedQuotes = editedQuotes.toMutableList().also {
                                            it.removeAt(index)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemeButton(
                        label = "ADD QUOTE",
                        selected = true,
                        background = themePalette.accent(isDark),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (editedQuotes.size < 100) {
                                editedQuotes = editedQuotes + ""
                            }
                        }
                    )
                    ThemeButton(
                        label = "SAVE",
                        selected = true,
                        background = BrutalColors.Lime,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCustomQuotesChange(editedQuotes)
                        }
                    )
                    ThemeButton(
                        label = "RESET",
                        selected = true,
                        background = BrutalColors.Orange,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editedQuotes = emptyList()
                            onCustomQuotesChange(emptyList())
                        }
                    )
                }
            }
        }

        SettingsSectionTitle("APP TILES")
        BrutalBlock(Modifier.fillMaxWidth(), background = BrutalColors.Lime, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("APP TILE CONTENT", fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("The selected content mode adapts to every tile size.", fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TileContentMode.ICON, TileContentMode.ICON_TEXT, TileContentMode.TEXT).forEach { mode ->
                        ThemeButton(when(mode) { TileContentMode.ICON -> "ICON"; TileContentMode.ICON_TEXT -> "ICON + TEXT"; TileContentMode.TEXT -> "TEXT" }, appTileContentMode == mode, when(mode) { TileContentMode.ICON -> BrutalColors.Cyan; TileContentMode.ICON_TEXT -> BrutalColors.Yellow; TileContentMode.TEXT -> BrutalColors.Pink }, Modifier.weight(1f)) { onAppTileContentModeChange(mode) }
                    }
                }
            }
        }

        SettingsSectionTitle("ICON STYLE")
        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    "APP ICON STYLE",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = uiOnSurface
                )
                Text(
                    "Controls how app icons are rendered across Home and Apps.",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = uiOnSurface.copy(alpha = .75f)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconThemeStyle.values().toList().chunked(3).forEach { rowStyles ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowStyles.forEach { style ->
                                ThemeButton(
                                    label = style.label,
                                    selected = iconThemeStyle == style,
                                    background = when (style) {
                                        IconThemeStyle.ORIGINAL -> BrutalColors.Cyan
                                        IconThemeStyle.MONOCHROME -> BrutalColors.Ink
                                        IconThemeStyle.ACCENT_TINTED -> LocalNeoThemePalette.current.accent(LocalNeoThemeIsDark.current)
                                        IconThemeStyle.TEXT_ONLY -> BrutalColors.Yellow
                                        IconThemeStyle.CIRCLE -> BrutalColors.Pink
                                        IconThemeStyle.ROUNDED_SQUARE -> BrutalColors.Lime
                                    },
                                    modifier = Modifier.weight(1f),
                                    onClick = { onIconThemeStyleChange(style) }
                                )
                            }
                            repeat(3 - rowStyles.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        SettingsSectionTitle("ICON PACK")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("THIRD-PARTY ICON PACKS", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal, color = uiOnSurface)
                Text("Standard launcher icon packs with appfilter.xml are supported. Icons without a matching pack entry keep the normal app icon.", fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                ThemeButton("SYSTEM ICONS", iconPackPackage == null, BrutalColors.Cyan, Modifier.fillMaxWidth()) { onIconPackChange(null) }
                if (iconPacks.isEmpty()) Text("NO COMPATIBLE ICON PACKS DETECTED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = uiOnSurface.copy(alpha = .6f))
                iconPacks.forEach { pack ->
                    ThemeButton(pack.label.uppercase(Locale.ENGLISH), iconPackPackage == pack.packageName, BrutalColors.Yellow, Modifier.fillMaxWidth()) { onIconPackChange(pack.packageName) }
                }
            }
        }

        SettingsSectionTitle("INTEGRATIONS")
        BrutalBlock(Modifier.fillMaxWidth(), background = if (uiBackground == BrutalColors.DarkPaper) BrutalColors.Purple else BrutalColors.Cyan, borderWidth = 4.dp, shadowX = 6.dp, shadowY = 6.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("MUSIC + NOTIFICATION ACCESS", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal, color = if (uiBackground == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink)
                Text(if (notificationAccessGranted) "NOTIFICATION ACCESS IS ENABLED FOR THE MUSIC / CHAT FEATURES." else "ENABLE ANDROID NOTIFICATION ACCESS FOR MUSIC AND LIVE CHAT.", fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = if (uiBackground == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink)
                BrutalActionButton(if (notificationAccessGranted) "OPEN NOTIFICATION ACCESS" else "ALLOW MUSIC / NOTIFICATION ACCESS", if (notificationAccessGranted) BrutalColors.Yellow else BrutalColors.Orange, onClick = onOpenNotificationAccess)
            }
        }
        BrutalBlock(Modifier.fillMaxWidth(), background = BrutalColors.Pink, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LIVE CHAT TILE", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal)
                Text(selectedChatApp?.label?.uppercase(Locale.ENGLISH) ?: "NO APP SELECTED", fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("The latest notification from this app appears on LIVE.", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrutalActionButton(if (selectedChatApp == null) "CHOOSE APP" else "CHANGE APP", BrutalColors.Yellow, Modifier.weight(1f)) { showChatAppPicker = true }
                    if (selectedChatApp != null) BrutalActionButton("CLEAR", BrutalColors.White, Modifier.weight(.7f)) { onChatNotificationPackagesChange(emptyList()) }
                }
            }
        }

        SettingsSectionTitle("PINNED APPS")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("$favoritesCount PINNED APPS", fontFamily = BrutalTypography.Display, fontSize = 18.sp, fontWeight = FontWeight.Normal, color = uiOnSurface)
                Text("Choose how many Home app tiles are available.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3,5,7).forEach { count -> ThemeButton(count.toString(), homeAppCount == count, if(count==3) BrutalColors.Cyan else if(count==5) BrutalColors.Orange else BrutalColors.Pink, Modifier.weight(1f)) { onHomeAppCountChange(count) } }
                }
                BrutalActionButton("CLEAR ALL PINNED APPS", BrutalColors.Orange, onClick = onClearFavorites)
            }
        }

        SettingsSectionTitle("WALLPAPER")
        BrutalBlock(Modifier.fillMaxWidth(), background = BrutalColors.Yellow, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (wallpaperUri == null) "NO WALLPAPER SELECTED" else "CUSTOM IMAGE SELECTED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrutalActionButton(
                        "CHOOSE IMAGE",
                        BrutalColors.Pink,
                        Modifier.weight(1f),
                        onChooseWallpaper
                    )
                    BrutalActionButton(
                        "CLEAR",
                        BrutalColors.White,
                        Modifier.weight(0.7f),
                        onClearWallpaper
                    )
                }
            }
        }

        SettingsSectionTitle("DATA & RECOVERY")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LOCAL BACKUP / RESTORE", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal, color = uiOnSurface)
                Text("Backup contains launcher settings, tile layout, pinned apps, notes/tasks and icon-pack selection. It does not contain passwords or notification contents.", fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrutalActionButton("BACKUP", BrutalColors.Cyan, Modifier.weight(1f), onBackup)
                    BrutalActionButton("RESTORE", BrutalColors.Yellow, Modifier.weight(1f), onRestore)
                }
                BrutalActionButton("RESET ALL CUSTOMIZATIONS", BrutalColors.Pink) { showResetConfirm = true }
            }
        }

        SettingsSectionTitle("LAUNCHER")
        BrutalBlock(Modifier.fillMaxWidth(), background = BrutalColors.Pink, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DEFAULT HOME APP", fontFamily = BrutalTypography.Display, fontSize = 17.sp, fontWeight = FontWeight.Normal)
                Text("Choose Neo Brutal Launcher as the Android default Home app.", fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
                BrutalActionButton("OPEN HOME SETTINGS", BrutalColors.Yellow) { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
        }

        SettingsSectionTitle("ABOUT")
        BrutalBlock(Modifier.fillMaxWidth(), background = darkTileBackground, borderWidth = 3.dp, shadowX = 5.dp, shadowY = 5.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("NEO BRUTAL LAUNCHER", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrutalColors.White)
                Text("CORE BUILD 0.1.0", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = BrutalColors.Cyan)
                Text("A neo-brutalist launcher focused on fast access to your apps.", fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = BrutalColors.White)
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showChatAppPicker) {
        AlertDialog(onDismissRequest = { showChatAppPicker = false }, title = { Text("CHOOSE CHAT APP", fontFamily = BrutalTypography.Display, fontWeight = FontWeight.Normal) }, text = {
            LazyColumn(modifier = Modifier.height(360.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(chatCandidates, key = { it.packageName + "/" + it.activityName }) { app ->
                    BrutalBlock(Modifier.fillMaxWidth().clickable { onChatNotificationPackagesChange(listOf(app.packageName)); showChatAppPicker = false }, background = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Yellow else uiSurface, borderWidth = 3.dp, shadowX = 3.dp, shadowY = 3.dp) {
                        Text(app.label.uppercase(Locale.ENGLISH), fontSize = 11.sp, fontWeight = FontWeight.Black, color = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Ink else uiOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }, confirmButton = {})
    }

    if (showResetConfirm) {
        AlertDialog(onDismissRequest = { showResetConfirm = false }, title = { Text("RESET ALL CUSTOMIZATIONS", fontFamily = BrutalTypography.Display) }, text = { Text("This clears launcher preferences, tile layout, pinned apps, notes/tasks, wallpaper and icon-pack selection. This cannot be undone.") }, dismissButton = { BrutalActionButton("CANCEL", BrutalColors.White) { showResetConfirm = false } }, confirmButton = { BrutalActionButton("RESET", BrutalColors.Pink) { showResetConfirm = false; onResetAll() } })
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 2.dp),
        fontFamily = BrutalTypography.Display,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    background: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    BrutalBlock(
        modifier = Modifier.fillMaxWidth(),
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = BrutalTypography.Display,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ThemeButton(
    label: String,
    selected: Boolean,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    BrutalBlock(
        modifier = modifier.clickable(onClick = onClick),
        background = if (selected) background else MaterialTheme.colorScheme.surface,
        borderWidth = if (selected) 4.dp else 2.dp,
        shadowX = if (selected) 4.dp else 3.dp,
        shadowY = if (selected) 4.dp else 3.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = BrutalTypography.Display,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = if (selected) {
                if (background.luminance() < 0.45f) BrutalColors.White else BrutalColors.Ink
            } else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BrutalActionButton(
    title: String,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    BrutalBlock(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        background = background,
        borderWidth = 3.dp,
        shadowX = 4.dp,
        shadowY = 4.dp
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = BrutalColors.Ink,
            letterSpacing = 0.5.sp
        )
    }
}
