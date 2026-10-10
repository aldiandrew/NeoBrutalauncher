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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
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
    var onboardingCompleted by remember { mutableStateOf(preferences.onboardingCompleted()) }

    var themePreference by remember { mutableStateOf(preferences.theme()) }
    var designPreset by remember { mutableStateOf(preferences.designPreset()) }
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
    var wallpaperUri by remember { mutableStateOf(preferences.wallpaperUri()) }
    var quoteImageUri by remember { mutableStateOf(preferences.quoteImageUri()) }
    var motionSmoothness by remember { mutableStateOf(preferences.motionSmoothness()) }
    var reduceMotion by remember { mutableStateOf(preferences.reduceMotion()) }
    var customQuotes by remember { mutableStateOf(preferences.customQuotes()) }
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

    val quoteImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            quoteImageUri = uri.toString()
            preferences.setQuoteImageUri(uri.toString())
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
                // AppRepository returns the same list instance while its cache is valid.
                // Avoid a structural list equality check on cache-hit refreshes.
                if (apps !== loadedApps) {
                    apps = loadedApps
                }
                if (preferences.onboardingCompleted() &&
                    !preferences.homeAppsInitialized() &&
                    favorites.isEmpty()
                ) {
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
            if (event == Lifecycle.Event.ON_RESUME) {
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
        smoothness = motionSmoothness,
        reduceMotion = reduceMotion
    )

    fun requestLaunch(app: AppInfo) {
        repository.launch(app)
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    when {
        !onboardingCompleted -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                designPreset = designPreset,
                typographyStyle = typographyStyle,
            ) {
                NeoOnboardingScreen(
                    apps = apps,
                    initialFavorites = favorites,
                    favoriteLimit = homeAppCount,
                    locationPermissionGranted = locationPermissionGranted,
                    notificationAccessGranted = notificationAccessGranted,
                    onRequestLocationPermission = {
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
                    onFavoritesChange = { updated ->
                        favorites = updated
                        preferences.setFavorites(updated)
                    },
                    onFinish = {
                        var selected = preferences.favorites()
                        if (selected.isEmpty()) {
                            selected = apps
                                .filter { it.packageName != context.packageName }
                                .take(homeAppCount)
                                .map { it.packageName + "/" + it.activityName }
                                .toSet()
                            favorites = selected
                            preferences.setFavorites(selected)
                        }
                        preferences.setHomeAppsInitialized(true)
                        preferences.setOnboardingCompleted(true)
                        onboardingCompleted = true
                    }
                )
            }
        }

        settingsOpen -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                designPreset = designPreset,
                typographyStyle = typographyStyle,
            ) {
                CompositionLocalProvider(LocalNeoMotionConfig provides motionConfig) {
                    SettingsScreen(
                    themePreference = themePreference,
                    designPreset = designPreset,
                    use24Hour = use24Hour,
                    showAmPm = showAmPm,
                    homeAppCount = homeAppCount,
                    showWeather = showWeather,
                    customQuotes = customQuotes,
                    appTileContentMode = appTileContentMode,
                    typographyStyle = typographyStyle,
                    iconPackPackage = iconPackPackage,
                    motionSmoothness = motionSmoothness,
                    reduceMotion = reduceMotion,
                    wallpaperUri = wallpaperUri,
                    quoteImageUri = quoteImageUri,
                    favorites = favorites,
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
                    onDesignPresetChange = {
                        designPreset = it
                        preferences.setDesignPreset(it)
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
                            3, 5, 7, 8 -> count
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
                    onTogglePinnedApp = { app ->
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
                    onCustomQuotesChange = {
                        customQuotes = it
                        preferences.setCustomQuotes(it)
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
                    onChooseQuoteImage = {
                        quoteImagePickerLauncher.launch(arrayOf("image/*"))
                    },
                    onClearQuoteImage = {
                        quoteImageUri = null
                        preferences.setQuoteImageUri(null)
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
                designPreset = designPreset,
                typographyStyle = typographyStyle,
            ) {
                CompositionLocalProvider(LocalNeoMotionConfig provides motionConfig) {
                    LauncherPageHost(
                        currentPage = currentPage,
                        motionConfig = motionConfig,
                        wallpaperUri = wallpaperUri,
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
                            quoteImageUri = quoteImageUri,
                            onChooseQuoteImage = { quoteImagePickerLauncher.launch(arrayOf("image/*")) },
                            onOpenSettings = { settingsOpen = true },
                            onOpenApps = { currentPage = 1 },
                            onLaunch = ::requestLaunch,
                            onHomeAppCountChange = { updated ->
                                homeAppCount = updated
                                preferences.setHomeAppCount(updated)
                            },
                            onRemovePinnedApp = { app ->
                                val key = app.packageName + "/" + app.activityName
                                val updated = favorites - key
                                favorites = updated
                                preferences.setFavorites(updated)
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
                            onLaunch = ::requestLaunch,
                            onOpenHome = { currentPage = 0 }
                        )
                    } else {
                        LivePage(
                            apps = apps,
                            customQuotes = customQuotes,
                            selectedChatPackages = selectedChatPackages,
                            showWeather = showWeather,
                            use24Hour = use24Hour,
                            showAmPm = showAmPm,
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
                }
            }
        }
    }
}

@Composable
private fun LauncherPageHost(
    currentPage: Int,
    motionConfig: NeoMotionConfig,
    wallpaperUri: String?,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BrutalWallpaper(
            uriString = wallpaperUri,
            modifier = Modifier.fillMaxSize()
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 0,
            userScrollEnabled = true,
            key = { it }
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                content(page)
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
                        .size(32.dp)
                        .clickable(
                            onClickLabel = when (index) {
                                0 -> "Open Home"
                                1 -> "Open Apps"
                                else -> "Open Live"
                            },
                            onClick = { onPageChange(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (pagerState.currentPage == index) BrutalColors.Yellow else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = BrutalColors.Yellow
                            )
                    )
                }
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
                color = BrutalColors.Yellow
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
    customQuotes: List<String>,
    appTileContentMode: TileContentMode,
    typographyStyle: TypographyStyle,
    wallpaperUri: String?,
    quoteImageUri: String?,
    onChooseQuoteImage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    onRemovePinnedApp: (AppInfo) -> Unit,
    tilePositions: Map<String, NeoTilePosition>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    tileSizes: Map<String, NeoTileSize>,
    onTileSizeChange: (String, NeoTileSize) -> Unit,
) {
    BackHandler(onBack = {})

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { LauncherPreferences(context) }
    val now = rememberMinuteClock()
    val homeClockBackground = BrutalColors.Yellow
    val homeClockText = BrutalColors.Ink
    val homeMusicBackground = if (MaterialTheme.colorScheme.background == BrutalColors.DarkPaper) BrutalColors.DarkTile else BrutalColors.Cyan
    val isDarkTheme = MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val quoteRotation = rememberLiveTileData(
        tileId = "home-quotes",
        refreshIntervalMillis = 30L * 60L * 1000L,
        initialValue = (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    ) {
        (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    }.value ?: 0

    var weatherRefreshToken by remember { mutableIntStateOf(0) }
    var taskItems by remember { mutableStateOf(preferences.taskItems()) }
    var selectedTile by remember { mutableStateOf<NeoTileSpec?>(null) }
    var tileEditMode by remember { mutableStateOf(false) }
    var excludedHomeApps by remember { mutableStateOf(preferences.excludedHomeApps()) }

    val timePattern = when {
        use24Hour -> "HH:mm"
        showAmPm -> "hh:mm a"
        else -> "hh:mm"
    }
    val time = remember(timePattern) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val longDay = remember { SimpleDateFormat("EEEE", Locale.ENGLISH) }
    val longDate = remember { SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH) }

    val appKeys = remember(apps) {
        apps.map { it.packageName + "/" + it.activityName }
    }
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
                    appKeys
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
    val launchableApps = remember(
        stableHomeOrder,
        excludedHomeApps,
        favorites,
        appsByKey,
        homeAppCount,
        tilePositions
    ) {
        stableHomeOrder
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
    }

    val palette = remember(BrutalColors.activePreset) { BrutalColors.appPalette(0) }
    val appTileIds = remember(launchableApps) {
        launchableApps.map { "app_" + it.packageName + "_" + it.activityName }.toSet() +
            setOf("home_music", "home_quote_image")
    }
    val homeQuote = NeoQuotes.pairForRotation(quoteRotation, customQuotes).first
    val homeQuoteHeight = when {
        homeQuote.length > 135 -> 112.dp
        homeQuote.length > 90 -> 96.dp
        homeQuote.length > 50 -> 84.dp
        else -> 76.dp
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
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "top-bar") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrutalBlock(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        background = BrutalColors.Cyan,
                        borderWidth = 3.dp,
                        shadowX = 4.dp,
                        shadowY = 4.dp
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "NEO",
                                fontFamily = BrutalTypography.Display,
                                fontSize = 23.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = BrutalColors.Ink
                            )
                            Text(
                                text = "BRUTAL LAUNCHER / 01",
                                fontSize = 8.sp,
                                lineHeight = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.7.sp,
                                color = BrutalColors.Ink
                            )
                        }
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

            item(key = "home-quote") {
                NeoQuoteTile(
                    quote = homeQuote,
                    modifier = Modifier.fillMaxWidth().height(homeQuoteHeight),
                    emphasized = true,
                    paletteIndex = quoteRotation
                )
            }

            item(key = "launchable-apps") {
                if (launchableApps.isNotEmpty()) {
                    NeoTileGrid(
                        tiles = buildList {
                            launchableApps.forEachIndexed { index, app ->
                                val id = "app_" + app.packageName + "_" + app.activityName
                                val defaultSize = when {
                                    index < 2 -> NeoTileSize.HORIZONTAL
                                    else -> NeoTileSize.SMALL
                                }
                                val size = tileSizes[id] ?: defaultSize
                                val stableVariant = appTileLayoutVariant(app, size)
                                val tileColor = palette[Math.floorMod(app.packageName.hashCode(), palette.size)]

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
                                            variant = stableVariant
                                        )
                                    }
                                )
                            }
                            add(
                                NeoTileSpec(
                                    id = "home_music",
                                    size = tileSizes["home_music"] ?: NeoTileSize.FOUR_BY_ONE,
                                    label = "MUSIC"
                                ) {
                                    NeoMusicTile(
                                        context = context,
                                        modifier = Modifier.fillMaxSize(),
                                        background = homeMusicBackground,
                                        textColor = if (isDarkTheme) BrutalColors.DarkWhite else BrutalColors.Ink,
                                        onLongClick = {
                                            selectedTile = NeoTileSpec(
                                                id = "home_music",
                                                size = tileSizes["home_music"] ?: NeoTileSize.FOUR_BY_ONE,
                                                label = "MUSIC"
                                            ) {}
                                        }
                                    )
                                }
                            )
                            add(
                                NeoTileSpec(
                                    id = "home_quote_image",
                                    size = NeoTileSize.FOUR_BY_TWO,
                                    label = "QUOTE + IMAGE"
                                ) {
                                    NeoQuoteImageTile(
                                        quote = homeQuote,
                                        imageUri = quoteImageUri,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            )
                        },
                        positions = tilePositions.filterKeys { appTileIds.contains(it) },
                        onPositionsChange = onTilePositionsChange,
                        onTileLongPress = { selectedTile = it },
                        onTileEdit = { selectedTile = it },
                        onTileMoveFinished = { tileEditMode = false },
                        editMode = tileEditMode,
                        modifier = Modifier.fillMaxWidth(),
                        gap = 10.dp
                    )
                }
            }

            item(key = "system-tiles") {
                NeoTileGrid(
                    tiles = buildList {
                        add(
                            NeoTileSpec(
                                id = "system_battery",
                                size = NeoTileSize.SMALL,
                                label = "BATTERY"
                            ) {
                                BatteryTile(
                                    context = context,
                                    modifier = Modifier.fillMaxSize(),
                                    background = BrutalColors.Yellow
                                )
                            }
                        )
                        add(
                            NeoTileSpec(
                                id = "system_network",
                                size = NeoTileSize.SMALL,
                                label = "NETWORK"
                            ) {
                                NeoNetworkTile(
                                    context = context,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )
                        if (showWeather) {
                            add(
                                NeoTileSpec(
                                    id = "system_weather",
                                    size = NeoTileSize.SMALL,
                                    label = "WEATHER",
                                    onClick = { weatherRefreshToken++ }
                                ) {
                                    NeoWeatherTile(
                                        context = context,
                                        refreshToken = weatherRefreshToken,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            )
                        }
                    },
                    positions = emptyMap(),
                    onPositionsChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    gap = 10.dp
                )
            }

        }
    }

    selectedTile?.let { tile ->
        val tileApp = launchableApps.firstOrNull {
            ("app_" + it.packageName + "_" + it.activityName) == tile.id
        }
        val isPinnedApp = tileApp?.let {
            favorites.contains(it.packageName + "/" + it.activityName)
        } == true

        AlertDialog(
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CURRENT SIZE: " + tile.size.label,
                        fontWeight = FontWeight.Black
                    )

                    if (tile.id != "home_quote_image") {
                    listOf(
                        NeoTileSize.SMALL,
                        NeoTileSize.HORIZONTAL,
                        NeoTileSize.THREE_BY_ONE,
                        NeoTileSize.FOUR_BY_ONE
                    ).forEach { option ->
                        BrutalActionButton(
                            title = "SET ${option.label}",
                            background = if (tile.size == option) BrutalColors.Yellow else BrutalColors.White
                        ) {
                            onTileSizeChange(tile.id, option)
                            selectedTile = null
                            tileEditMode = false
                        }
                    }
                    } else {
                        BrutalActionButton(
                            title = "CHOOSE IMAGE",
                            background = BrutalColors.Yellow
                        ) {
                            onChooseQuoteImage()
                            selectedTile = null
                            tileEditMode = false
                        }
                    }

                    BrutalActionButton(
                        title = "MOVE TILE",
                        background = BrutalColors.Cyan
                    ) {
                        selectedTile = null
                        tileEditMode = true
                    }

                    if (isPinnedApp && tileApp != null) {
                        BrutalActionButton(
                            title = "REMOVE PIN",
                            background = BrutalColors.Pink
                        ) {
                            onRemovePinnedApp(tileApp)
                            selectedTile = null
                            tileEditMode = false
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        selectedTile = null
                        tileEditMode = false
                    }
                ) {
                    Text("DONE", fontWeight = FontWeight.Black)
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
    paletteIndex: Int = 0,
    showLabel: Boolean = false
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
                val availableHeight = (
                    maxHeight.value -
                        (if (emphasized) 20f else 30f) -
                        if (showLabel) 12f else 0f
                    ).coerceAtLeast(18f)
                var quoteSize = if (emphasized) {
                    val heightFactor = if (quote.length <= 55) 0.58f else 0.43f
                    minOf(34f, maxWidth.value * 0.11f, availableHeight * heightFactor)
                } else {
                    when {
                        compact < 78.dp -> 7f
                        compact < 155.dp -> 12f
                        else -> 17f
                    }
                }.coerceAtLeast(7f)

                while (quoteSize > 7f) {
                    val estimatedCharsPerLine =
                        (maxWidth.value / (quoteSize * 0.58f)).toInt().coerceAtLeast(8)
                    val estimatedLines =
                        ((quote.length + estimatedCharsPerLine - 1) / estimatedCharsPerLine)
                            .coerceAtLeast(1)
                    val neededHeight = estimatedLines * quoteSize * 1.08f
                    if (neededHeight <= availableHeight) break
                    quoteSize -= 0.5f
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
                    if (showLabel) {
                        Text(
                            text = "QUOTE",
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = palette.text,
                            maxLines = 1
                        )
                    }
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
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
    }
}

private fun appTileLayoutVariant(app: AppInfo, tileSize: NeoTileSize): Int {
    // Stable per app + size: no random layout changes across recomposition or restarts.
    val layoutKey = app.packageName + "/" + app.activityName + ":" + tileSize.name
    return Math.floorMod(layoutKey.hashCode(), 5)
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
        // Capture the tile constraints before entering nested Box/Row content scopes.
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val iconBitmap = remember(app.packageName, app.icon) {
            app.icon.toBitmap(64, 64).asImageBitmap()
        }
        val compactTile = tileSize == NeoTileSize.SMALL
        val iconSize = when (tileSize) {
            NeoTileSize.SMALL -> minOf(availableWidth * 0.34f, availableHeight * 0.36f, 26.dp)
            NeoTileSize.HORIZONTAL -> minOf(availableHeight * 0.38f, 30.dp)
            NeoTileSize.THREE_BY_ONE -> minOf(availableHeight * 0.42f, 34.dp)
            NeoTileSize.FOUR_BY_ONE, NeoTileSize.FOUR_BY_TWO -> minOf(availableHeight * 0.72f, 72.dp)
        }
        val maxTextSize = when (tileSize) {
            NeoTileSize.SMALL -> 9.sp
            NeoTileSize.HORIZONTAL -> 15.sp
            NeoTileSize.THREE_BY_ONE -> if (availableWidth < 220.dp) 18.sp else 23.sp
            NeoTileSize.FOUR_BY_ONE, NeoTileSize.FOUR_BY_TWO -> if (availableWidth < 300.dp) 23.sp else 30.sp
        }
        val textMaxLines = 2

        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = background,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            Box(Modifier.fillMaxSize()) {
                when (contentMode) {
                    TileContentMode.ICON -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    }

                    TileContentMode.TEXT -> {
                        val isFourByOne = tileSize == NeoTileSize.FOUR_BY_ONE || tileSize == NeoTileSize.FOUR_BY_TWO
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = if (isFourByOne) Alignment.CenterEnd else if (compactTile) Alignment.Center else Alignment.CenterStart
                        ) {
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (isFourByOne) TextAlign.End else if (compactTile) TextAlign.Center else TextAlign.Start,
                                fontFamily = BrutalTypography.Display,
                                fontSize = maxTextSize,
                                lineHeight = (maxTextSize.value * 1.02f).sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = textMaxLines,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    TileContentMode.ICON_TEXT -> {
                        val appTitle: @Composable (Modifier, TextAlign, Int) -> Unit =
                            { textModifier, alignment, lines ->
                                Text(
                                    text = app.label.uppercase(),
                                    modifier = textModifier,
                                    textAlign = alignment,
                                    fontFamily = BrutalTypography.Display,
                                    fontSize = maxTextSize,
                                    lineHeight = (maxTextSize.value * 1.02f).sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink,
                                    maxLines = lines,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        val appIcon: @Composable (Modifier) -> Unit = { iconModifier ->
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = iconModifier.size(iconSize)
                            )
                        }
                        val layout = Math.floorMod(variant, 5)

                        when (tileSize) {
                            NeoTileSize.SMALL -> {
                                // Match the compact Wi-Fi tile: centered icon, readable two-line label.
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    appIcon(Modifier)
                                    appTitle(
                                        Modifier.fillMaxWidth(),
                                        TextAlign.Center,
                                        2
                                    )
                                }
                            }

                            NeoTileSize.HORIZONTAL -> {
                                if (availableHeight < 66.dp || availableWidth < 130.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        appIcon(Modifier)
                                        appTitle(Modifier.weight(1f), TextAlign.Start, 1)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        when (layout) {
                                            0 -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appIcon(Modifier)
                                                appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                            }
                                            1 -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                                appIcon(Modifier)
                                            }
                                            2 -> {
                                                appIcon(Modifier.align(Alignment.TopStart))
                                                appTitle(
                                                    Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.78f),
                                                    TextAlign.End,
                                                    1
                                                )
                                            }
                                            3 -> {
                                                appTitle(
                                                    Modifier.align(Alignment.TopStart).fillMaxWidth(0.78f),
                                                    TextAlign.Start,
                                                    1
                                                )
                                                appIcon(Modifier.align(Alignment.BottomEnd))
                                            }
                                            else -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appIcon(Modifier)
                                                appTitle(Modifier.weight(1f), TextAlign.Center, 2)
                                            }
                                        }
                                    }
                                }
                            }

                            NeoTileSize.THREE_BY_ONE,
                            NeoTileSize.FOUR_BY_ONE, NeoTileSize.FOUR_BY_TWO -> {
                                // Width/height checks keep the selected composition from colliding.
                                if (availableHeight < 66.dp || availableWidth < 190.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        appIcon(Modifier)
                                        appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(
                                                horizontal = if (tileSize == NeoTileSize.FOUR_BY_ONE || tileSize == NeoTileSize.FOUR_BY_TWO) 14.dp else 9.dp,
                                                vertical = 7.dp
                                            )
                                    ) {
                                        when (layout) {
                                            0 -> Row(
                                                Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                appIcon(Modifier)
                                                appTitle(Modifier.weight(1f), TextAlign.Start, 2)
                                            }
                                            1 -> {
                                                appTitle(
                                                    Modifier.align(Alignment.CenterStart)
                                                        .fillMaxWidth(0.76f)
                                                        .padding(end = iconSize + 8.dp),
                                                    TextAlign.Start,
                                                    2
                                                )
                                                appIcon(Modifier.align(Alignment.CenterEnd))
                                            }
                                            2 -> {
                                                appIcon(Modifier.align(Alignment.TopEnd))
                                                appTitle(
                                                    Modifier.align(Alignment.BottomStart)
                                                        .fillMaxWidth(0.82f)
                                                        .padding(end = iconSize + 8.dp),
                                                    TextAlign.Start,
                                                    2
                                                )
                                            }
                                            3 -> {
                                                appTitle(
                                                    Modifier.align(Alignment.TopStart)
                                                        .fillMaxWidth(0.82f)
                                                        .padding(end = iconSize + 8.dp),
                                                    TextAlign.Start,
                                                    2
                                                )
                                                appIcon(Modifier.align(Alignment.BottomEnd))
                                            }
                                            else -> {
                                                appIcon(Modifier.align(Alignment.BottomStart))
                                                appTitle(
                                                    Modifier.align(Alignment.TopEnd)
                                                        .fillMaxWidth(0.78f)
                                                        .padding(start = iconSize + 8.dp),
                                                    TextAlign.End,
                                                    2
                                                )
                                            }
                                        }
                                    }
                                }
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
    designPreset: DesignPreset,
    use24Hour: Boolean,
    showAmPm: Boolean,
    homeAppCount: Int,
    showWeather: Boolean,
    customQuotes: List<String>,
    appTileContentMode: TileContentMode,
    typographyStyle: TypographyStyle,
    iconPackPackage: String?,
    motionSmoothness: MotionSmoothness,
    reduceMotion: Boolean,
    wallpaperUri: String?,
    quoteImageUri: String?,
    favorites: Set<String>,
    favoritesCount: Int,
    locationPermissionGranted: Boolean,
    notificationAccessGranted: Boolean,
    onChatNotificationPackagesChange: (List<String>) -> Unit,
    onBack: () -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onDesignPresetChange: (DesignPreset) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onShowAmPmChange: (Boolean) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    onShowWeatherChange: (Boolean) -> Unit,
    onRequestWeatherPermission: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onAppTileContentModeChange: (TileContentMode) -> Unit,
    onTypographyStyleChange: (TypographyStyle) -> Unit,
    onIconPackChange: (String?) -> Unit,
    onTogglePinnedApp: (AppInfo) -> Unit,
    onCustomQuotesChange: (List<String>) -> Unit,
    onMotionSmoothnessChange: (MotionSmoothness) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onChooseWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    onChooseQuoteImage: () -> Unit,
    onClearQuoteImage: () -> Unit,
    onClearFavorites: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onResetAll: () -> Unit
) {
    var editedQuotes by remember(customQuotes) { mutableStateOf(customQuotes) }
    var selectedCategory by remember { mutableStateOf("General") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiBackground = MaterialTheme.colorScheme.background
    val uiSurface = MaterialTheme.colorScheme.surface
    val uiOnSurface = MaterialTheme.colorScheme.onSurface
    val isDark = uiBackground == BrutalColors.DarkPaper
    val iconPacks = remember { IconPackManager(context).installedIconPacks() }
    val chatCandidates = remember(apps) {
        apps.groupBy { it.packageName }.values.mapNotNull { it.firstOrNull() }
            .filter { it.packageName != context.packageName }.sortedBy { it.label.lowercase() }
    }
    var showChatAppPicker by remember { mutableStateOf(false) }
    var showIconPackPicker by remember { mutableStateOf(false) }
    var showPinnedAppPicker by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    val selectedChatApp = chatCandidates.firstOrNull { it.packageName == chatNotificationPackages.firstOrNull() }
    val selectedIconPack = iconPacks.firstOrNull { it.packageName == iconPackPackage }
    val pinnedCandidates = remember(apps) {
        apps.groupBy { it.packageName }
            .values
            .mapNotNull { it.firstOrNull() }
            .filter { it.packageName != context.packageName }
            .sortedBy { it.label.lowercase() }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(uiBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .padding(
            horizontal = NeoBrutalTokens.Spacing.Medium,
            vertical = NeoBrutalTokens.Spacing.Medium
        )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(NeoBrutalTokens.Spacing.Small)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = uiOnSurface) }
            BrutalBlock(modifier = Modifier.weight(1f), background = uiSurface, borderWidth = NeoBrutalTokens.Border.Primary, borderColor = uiOnSurface, shadowX = 3.dp, shadowY = 3.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
                Text("SETTINGS", fontFamily = BrutalTypography.Display, fontSize = NeoBrutalTokens.Type.Title, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("General", "Home", "Apps", "Live").forEach { category ->
                SettingsOptionButton(
                    label = category,
                    selected = selectedCategory == category,
                    background = BrutalColors.Yellow,
                    modifier = Modifier.weight(1f)
                ) { selectedCategory = category }
            }
        }

        when (selectedCategory) {
            "General" -> {
        SettingsSectionTitle("APPEARANCE")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color mode", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text("Choose the launcher color mode.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsOptionButton("SYSTEM", themePreference == ThemePreference.SYSTEM, BrutalColors.Cyan, Modifier.weight(1f)) { onThemeChange(ThemePreference.SYSTEM) }
                    SettingsOptionButton("LIGHT", themePreference == ThemePreference.LIGHT, BrutalColors.Yellow, Modifier.weight(1f)) { onThemeChange(ThemePreference.LIGHT) }
                    SettingsOptionButton("DARK", themePreference == ThemePreference.DARK, BrutalColors.Pink, Modifier.weight(1f)) { onThemeChange(ThemePreference.DARK) }
                }
            }
        }

        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color preset", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                DesignPreset.values().toList().chunked(2).forEach { rowPresets ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowPresets.forEach { preset ->
                            val presetColor = when (preset) {
                                DesignPreset.NEO_BRUTAL_CLASSIC -> Color(0xFFFFE500)
                                DesignPreset.ACID_DARK -> Color(0xFFC5FF00)
                                DesignPreset.COBALT_POP -> Color(0xFF9CB4FF)
                                DesignPreset.MONOCHROME -> Color(0xFFB8B8B8)
                            }
                            SettingsOptionButton(
                                preset.label,
                                designPreset == preset,
                                presetColor,
                                Modifier.weight(1f)
                            ) { onDesignPresetChange(preset) }
                        }
                    }
                }
            }
        }

        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Typography", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TypographyStyle.DEFAULT, TypographyStyle.CONDENSED).forEach { style ->
                        SettingsOptionButton(
                            style.label,
                            typographyStyle == style,
                            BrutalColors.Cyan,
                            Modifier.weight(1f)
                        ) {
                            onTypographyStyleChange(style)
                        }
                    }
                }
            }
        }

        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (wallpaperUri == null) "NO WALLPAPER SELECTED" else "CUSTOM IMAGE SELECTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton(
                        "CHOOSE IMAGE",
                        BrutalColors.Pink,
                        Modifier.weight(1f),
                        onChooseWallpaper
                    )
                    SettingsActionButton(
                        "CLEAR",
                        BrutalColors.White,
                        Modifier.weight(0.7f),
                        onClearWallpaper
                    )
                }
            }
        }

        SettingsSectionTitle("CLOCK")
        SettingsSwitch("24-HOUR TIME", "Use 24-hour time on Home.", use24Hour, BrutalColors.Yellow, onUse24HourChange)
        if (!use24Hour) SettingsSwitch("AM / PM", "Show the AM/PM marker with 12-hour time.", showAmPm, BrutalColors.Cyan, onShowAmPmChange)
        SettingsSectionTitle("MOTION")
        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Animation smoothness", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Controls how quickly launcher movement settles.", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MotionSmoothness.values().forEach { smoothness ->
                        SettingsOptionButton(
                            label = smoothness.label,
                            selected = motionSmoothness == smoothness,
                            background = BrutalColors.Cyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onMotionSmoothnessChange(smoothness) }
                        )
                    }
                }
            }
        }
        SettingsSwitch(
            "REDUCE MOTION",
            "Disable decorative launch and return movement.",
            reduceMotion,
            BrutalColors.Cyan,
            onReduceMotionChange
        )

        SettingsSectionTitle("DATA & RECOVERY")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backup and restore", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text("Backup contains launcher settings, tile layout, pinned apps, notes/tasks and icon-pack selection. It does not contain passwords or notification contents.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .75f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton("BACKUP", BrutalColors.Cyan, Modifier.weight(1f), onBackup)
                    SettingsActionButton("RESTORE", BrutalColors.Yellow, Modifier.weight(1f), onRestore)
                }
                SettingsActionButton("RESET ALL CUSTOMIZATIONS", BrutalColors.Pink) { showResetConfirm = true }
            }
        }

        SettingsSectionTitle("LAUNCHER")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Default Home app", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Choose Neo Brutal Launcher as the Android default Home app.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
                SettingsActionButton("OPEN HOME SETTINGS", BrutalColors.Yellow) { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
        }

        SettingsSectionTitle("ABOUT")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Neo Brutal Launcher", fontSize = 18.sp, fontWeight = FontWeight.Black, color = uiOnSurface)
                Text("Core build 0.1.0", fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = uiOnSurface.copy(alpha = .75f))
                Text("A neo-brutalist launcher focused on fast access to your apps.", fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = uiOnSurface)
            }
        }

            }
            "Home" -> {
        SettingsSectionTitle("Home screen")
        SettingsSwitch("WEATHER", "Show local weather. Location permission is required.", showWeather, BrutalColors.Cyan, onShowWeatherChange)
        if (!locationPermissionGranted) SettingsActionButton("ALLOW WEATHER LOCATION", BrutalColors.Yellow, onClick = onRequestWeatherPermission)

        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App tile content", fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("The selected content mode adapts to every tile size.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(TileContentMode.ICON, TileContentMode.ICON_TEXT, TileContentMode.TEXT).forEach { mode ->
                        SettingsOptionButton(when(mode) { TileContentMode.ICON -> "ICON"; TileContentMode.ICON_TEXT -> "ICON + TEXT"; TileContentMode.TEXT -> "TEXT" }, appTileContentMode == mode, when(mode) { TileContentMode.ICON -> BrutalColors.Cyan; TileContentMode.ICON_TEXT -> BrutalColors.Yellow; TileContentMode.TEXT -> BrutalColors.Pink }, Modifier.weight(1f)) { onAppTileContentModeChange(mode) }
                    }
                }
            }
        }

        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    "$favoritesCount PINNED APPS",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = uiOnSurface
                )
                Text(
                    "Choose how many Home app tiles are available.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = uiOnSurface.copy(alpha = .75f)
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 7).forEach { count ->
                        SettingsOptionButton(
                            count.toString(),
                            homeAppCount == count,
                            if (count == 3) BrutalColors.Cyan
                            else if (count == 5) BrutalColors.Yellow
                            else BrutalColors.Pink,
                            Modifier.weight(1f)
                        ) {
                            onHomeAppCountChange(count)
                        }
                    }
                }
                SettingsActionButton(
                    "CHOOSE PINNED APPS",
                    BrutalColors.Cyan,
                    Modifier.fillMaxWidth()
                ) {
                    showPinnedAppPicker = true
                }
                SettingsActionButton(
                    "CLEAR ALL PINNED APPS",
                    BrutalColors.Yellow,
                    onClick = onClearFavorites
                )
            }
        }

        SettingsSectionTitle("CONTENT")
        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Built-in quotes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = uiOnSurface)
                NeoQuotes.builtInQuotes().forEachIndexed { index, quote ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${index + 1}.",
                            modifier = Modifier.padding(end = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = uiOnSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = quote,
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = uiOnSurface
                        )
                    }
                }
            }
        }

        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Custom quotes", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = uiOnSurface)
                Text(
                    "${editedQuotes.size} CUSTOM / ${NeoQuotes.allQuotes(editedQuotes).size} TOTAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = uiOnSurface.copy(alpha = .7f)
                )
                if (editedQuotes.isEmpty()) {
                    Text("No custom quotes. Built-in quotes are still available.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = uiOnSurface.copy(alpha = .7f))
                } else {
                    editedQuotes.forEachIndexed { index, quote ->
                        val quotePalette = listOf(uiSurface)
                        BrutalBlock(
                            Modifier.fillMaxWidth(),
                            background = quotePalette[index % quotePalette.size],
                            borderWidth = 2.dp,
                            shadowX = 0.dp,
                            shadowY = 0.dp
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                Text(
                                    text = (index + 1).toString() + ".",
                                    modifier = Modifier.padding(start = 7.dp, top = 10.dp),
                                    fontFamily = BrutalTypography.Display,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = uiOnSurface
                                )
                                BasicTextField(
                                    value = quote,
                                    onValueChange = { value ->
                                        editedQuotes = editedQuotes.toMutableList().also { it[index] = value.take(300) }
                                    },
                                    modifier = Modifier.weight(1f).padding(horizontal = 7.dp, vertical = 8.dp),
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
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = uiOnSurface.copy(alpha = .6f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                SettingsOptionButton(
                                    label = "DELETE",
                                    selected = true,
                                    background = BrutalColors.Pink,
                                    modifier = Modifier.width(66.dp).padding(end = 5.dp, top = 6.dp),
                                    onClick = { editedQuotes = editedQuotes.filterIndexed { quoteIndex, _ -> quoteIndex != index } }
                                )
                            }
                        }
                    }
                }
                if (editedQuotes.size < 5) {
                    SettingsActionButton("ADD QUOTE", BrutalColors.Cyan) { editedQuotes = editedQuotes + "" }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton("SAVE QUOTES", BrutalColors.Yellow, Modifier.weight(1f)) {
                        editedQuotes = editedQuotes.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(5)
                        onCustomQuotesChange(editedQuotes)
                    }
                    SettingsActionButton("RESET", BrutalColors.White, Modifier.weight(.7f)) {
                        editedQuotes = emptyList()
                        onCustomQuotesChange(emptyList())
                    }
                }
            }
        }


        SettingsSectionTitle("Home quote + image tile")
        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (quoteImageUri == null) "No image selected" else "Custom image selected",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = uiOnSurface
                )
                Text(
                    text = "Choose the image shown in the right half of the 4×2 Home tile. It is displayed in black and white.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = uiOnSurface.copy(alpha = 0.78f)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton("Choose image", BrutalColors.Yellow, Modifier.weight(1f), onChooseQuoteImage)
                    if (quoteImageUri != null) {
                        SettingsActionButton("Clear", uiSurface, Modifier.weight(0.6f), onClearQuoteImage)
                    }
                }
            }
        }


            }
            "Apps" -> {
        SettingsSectionTitle("App icons")
        BrutalBlock(
            Modifier.fillMaxWidth(),
            background = uiSurface,
            borderWidth = 2.dp,
            shadowX = 0.dp,
            shadowY = 0.dp,
            shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = selectedIconPack?.label?.uppercase(Locale.ENGLISH) ?: "SYSTEM ICONS",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = uiOnSurface
                )
                Text(
                    text = "Choose the icon pack used by Home and Apps.",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = uiOnSurface.copy(alpha = .75f)
                )
                SettingsActionButton(
                    title = "CHOOSE ICON PACK",
                    background = BrutalColors.Yellow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    showIconPackPicker = true
                }
            }
        }


            }
            else -> {
        SettingsSectionTitle("INTEGRATIONS")
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Music and notification access", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = if (uiBackground == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink)
                Text(if (notificationAccessGranted) "NOTIFICATION ACCESS IS ENABLED FOR THE MUSIC / CHAT FEATURES." else "ENABLE ANDROID NOTIFICATION ACCESS FOR MUSIC AND LIVE CHAT.", fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = if (uiBackground == BrutalColors.DarkPaper) BrutalColors.DarkWhite else BrutalColors.Ink)
                SettingsActionButton(if (notificationAccessGranted) "OPEN NOTIFICATION ACCESS" else "ALLOW MUSIC / NOTIFICATION ACCESS", if (notificationAccessGranted) BrutalColors.Yellow else BrutalColors.Yellow, onClick = onOpenNotificationAccess)
            }
        }
        BrutalBlock(Modifier.fillMaxWidth(), background = uiSurface, borderWidth = 2.dp, shadowX = 0.dp, shadowY = 0.dp, shadowColor = if (isDark) BrutalColors.Yellow else BrutalColors.Ink) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Live chat tile", fontFamily = BrutalTypography.Display, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(selectedChatApp?.label?.uppercase(Locale.ENGLISH) ?: "NO APP SELECTED", fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("The latest notification from this app appears on LIVE.", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsActionButton(if (selectedChatApp == null) "CHOOSE APP" else "CHANGE APP", BrutalColors.Yellow, Modifier.weight(1f)) { showChatAppPicker = true }
                    if (selectedChatApp != null) SettingsActionButton("CLEAR", BrutalColors.White, Modifier.weight(.7f)) { onChatNotificationPackagesChange(emptyList()) }
                }
            }
        }


            }
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showIconPackPicker) {
        AlertDialog(
            onDismissRequest = { showIconPackPicker = false },
            title = {
                Text(
                    text = "CHOOSE ICON PACK",
                    fontFamily = BrutalTypography.Display,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = minOf(360.dp, (LocalConfiguration.current.screenHeightDp * 0.55f).dp)),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        val selected = iconPackPackage == null
                        BrutalBlock(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onIconPackChange(null)
                                    showIconPackPicker = false
                                },
                            background = if (selected) BrutalColors.Yellow else uiBackground,
                            borderWidth = 3.dp,
                            shadowX = if (selected) 0.dp else 3.dp,
                            shadowY = if (selected) 0.dp else 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SYSTEM ICONS",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selected) "SELECTED" else "USE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface
                                )
                            }
                        }
                    }
                    items(
                        items = iconPacks,
                        key = { it.packageName }
                    ) { pack ->
                        val selected = iconPackPackage == pack.packageName
                        BrutalBlock(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onIconPackChange(pack.packageName)
                                    showIconPackPicker = false
                                },
                            background = if (selected) BrutalColors.Yellow else uiBackground,
                            borderWidth = 3.dp,
                            shadowX = if (selected) 0.dp else 3.dp,
                            shadowY = if (selected) 0.dp else 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pack.label.uppercase(Locale.ENGLISH),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selected) "SELECTED" else "USE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) BrutalColors.Ink else uiOnSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showPinnedAppPicker) {
        AlertDialog(
            onDismissRequest = { showPinnedAppPicker = false },
            title = {
                Text(
                    text = "CHOOSE PINNED APPS",
                    fontFamily = BrutalTypography.Display,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = favorites.size.toString() + " / " + homeAppCount + " SELECTED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (pinnedCandidates.isEmpty()) {
                        Text(
                            text = "NO LAUNCHABLE APPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = minOf(360.dp, (LocalConfiguration.current.screenHeightDp * 0.55f).dp)),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(
                                items = pinnedCandidates,
                                key = { it.packageName + "/" + it.activityName }
                            ) { app ->
                                val key = app.packageName + "/" + app.activityName
                                val selected = favorites.contains(key)
                                val enabled = selected || favorites.size < homeAppCount
                                BrutalBlock(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            enabled = enabled,
                                            onClick = { onTogglePinnedApp(app) }
                                        ),
                                    background = if (selected) BrutalColors.Yellow else uiBackground,
                                    borderWidth = 3.dp,
                                    shadowX = if (selected) 0.dp else 3.dp,
                                    shadowY = if (selected) 0.dp else 3.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = app.label.uppercase(Locale.ENGLISH),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = uiOnSurface.copy(alpha = if (enabled) 1f else .45f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (selected) "SELECTED" else "ADD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = uiOnSurface.copy(alpha = if (enabled) 1f else .45f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showChatAppPicker) {
        AlertDialog(onDismissRequest = { showChatAppPicker = false }, title = { Text("CHOOSE CHAT APP", fontFamily = BrutalTypography.Display, fontWeight = FontWeight.SemiBold) }, text = {
            LazyColumn(modifier = Modifier.heightIn(max = minOf(360.dp, (LocalConfiguration.current.screenHeightDp * 0.55f).dp)), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(chatCandidates, key = { it.packageName + "/" + it.activityName }) { app ->
                    BrutalBlock(Modifier.fillMaxWidth().clickable { onChatNotificationPackagesChange(listOf(app.packageName)); showChatAppPicker = false }, background = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Yellow else uiSurface, borderWidth = 3.dp, shadowX = 3.dp, shadowY = 3.dp) {
                        Text(app.label.uppercase(Locale.ENGLISH), fontSize = 12.sp, fontWeight = FontWeight.Black, color = if(app.packageName == chatNotificationPackages.firstOrNull()) BrutalColors.Ink else uiOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }, confirmButton = {})
    }

    if (showResetConfirm) {
        AlertDialog(onDismissRequest = { showResetConfirm = false }, title = { Text("RESET ALL CUSTOMIZATIONS", fontFamily = BrutalTypography.Display) }, text = { Text("This clears launcher preferences, tile layout, pinned apps, notes/tasks, wallpaper and icon-pack selection. This cannot be undone.") }, dismissButton = { SettingsActionButton("CANCEL", BrutalColors.White) { showResetConfirm = false } }, confirmButton = { SettingsActionButton("RESET", BrutalColors.Pink) { showResetConfirm = false; onResetAll() } })
    }
}

@Composable
private fun SettingsActionButton(
    title: String,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val normalizedTitle = title.uppercase(Locale.getDefault())
    val destructive = background == BrutalColors.Pink ||
        normalizedTitle.startsWith("CLEAR") || normalizedTitle.contains("RESET") ||
        normalizedTitle == "DELETE" || normalizedTitle.startsWith("REMOVE")
    val neutral = !destructive && title == "CANCEL"
    val resolvedBackground = when {
        destructive -> BrutalColors.Pink
        neutral -> surface
        else -> BrutalColors.Yellow
    }
    BrutalPressableBlock(
        modifier = modifier.fillMaxWidth(),
        background = resolvedBackground,
        borderWidth = 2.dp,
        shadowX = 0.dp,
        shadowY = 0.dp,
        onClick = onClick
    ) {
        Text(
            text = title.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (neutral) onSurface else BrutalColors.Ink,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    val readableTitle = title.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
    Text(
        text = readableTitle,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SettingsOptionButton(
    label: String,
    selected: Boolean,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    BrutalPressableBlock(
        modifier = modifier,
        background = if (selected) {
            if (label in setOf("NEO-BRUTAL CLASSIC", "ACID DARK", "COBALT POP", "MONOCHROME")) {
                background
            } else {
                BrutalColors.Yellow
            }
        } else {
            MaterialTheme.colorScheme.surface
        },
        borderWidth = 2.dp,
        shadowX = 0.dp,
        shadowY = 0.dp,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) BrutalColors.Ink else MaterialTheme.colorScheme.onSurface
        )
    }
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
        background = MaterialTheme.colorScheme.surface,
        borderWidth = 2.dp,
        shadowX = 0.dp,
        shadowY = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.lowercase(Locale.getDefault()).split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } },
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            BrutalToggle(
                checked = checked,
                accent = background,
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
    BrutalPressableBlock(
        modifier = modifier,
        background = if (selected) background else MaterialTheme.colorScheme.surface,
        borderWidth = if (selected) NeoBrutalTokens.Border.Strong else NeoBrutalTokens.Border.Secondary,
        shadowX = NeoBrutalTokens.Shadow.Medium,
        shadowY = NeoBrutalTokens.Shadow.Medium,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = BrutalTypography.Display,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = if (selected) BrutalColors.Ink else MaterialTheme.colorScheme.onSurface
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
    BrutalPressableBlock(
        modifier = modifier.fillMaxWidth(),
        background = background,
        borderWidth = NeoBrutalTokens.Border.Primary,
        shadowX = NeoBrutalTokens.Shadow.Medium,
        shadowY = NeoBrutalTokens.Shadow.Medium,
        onClick = onClick
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
