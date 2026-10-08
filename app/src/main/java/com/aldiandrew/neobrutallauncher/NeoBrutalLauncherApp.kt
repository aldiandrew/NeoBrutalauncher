package com.aldiandrew.neobrutallauncher

import android.Manifest
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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
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
import kotlin.math.abs
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
    var tileEditMode by remember { mutableStateOf(false) }

    var themePreference by remember { mutableStateOf(preferences.theme()) }
    var use24Hour by remember { mutableStateOf(preferences.use24Hour()) }
    var showDate by remember { mutableStateOf(preferences.showDate()) }
    var homeAppCount by remember { mutableStateOf(preferences.homeAppCount()) }
    var showTagline by remember { mutableStateOf(preferences.showTagline()) }
    var showAppCount by remember { mutableStateOf(preferences.showAppCount()) }
    var showWeather by remember { mutableStateOf(preferences.showWeather()) }
    var showQuote by remember { mutableStateOf(preferences.showQuote()) }
    var showBattery by remember { mutableStateOf(preferences.showBattery()) }
    var favorites by remember { mutableStateOf(preferences.favorites()) }
    var tilePositions by remember { mutableStateOf(preferences.tilePositions()) }
    var tileSizes by remember { mutableStateOf(preferences.tileSizes()) }
    var appTileContentMode by remember { mutableStateOf(preferences.appTileContentMode()) }
    var brutalityLevel by remember { mutableStateOf(preferences.brutalityLevel()) }
    var clockStyle by remember { mutableStateOf(preferences.clockStyle()) }
    var wallpaperUri by remember { mutableStateOf(preferences.wallpaperUri()) }
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

    LaunchedEffect(Unit) {
        refreshApps()
    }

    when {
        settingsOpen -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                brutalityLevel = brutalityLevel,
            ) {
                SettingsScreen(
                    appsCount = apps.size,
                    themePreference = themePreference,
                    use24Hour = use24Hour,
                    showDate = showDate,
                    homeAppCount = homeAppCount,
                    showTagline = showTagline,
                    showAppCount = showAppCount,
                    showWeather = showWeather,
                    showQuote = showQuote,
                    showBattery = showBattery,
                    appTileContentMode = appTileContentMode,
                    brutalityLevel = brutalityLevel,
                    clockStyle = clockStyle,
                    wallpaperUri = wallpaperUri,
                    favoritesCount = favorites.size,
                    locationPermissionGranted = locationPermissionGranted,
                    notificationAccessGranted = notificationAccessGranted,
                    onBack = { settingsOpen = false },
                    onThemeChange = {
                        themePreference = it
                        preferences.setTheme(it)
                    },
                    onUse24HourChange = {
                        use24Hour = it
                        preferences.setUse24Hour(it)
                    },
                    onShowDateChange = {
                        showDate = it
                        preferences.setShowDate(it)
                    },
                    onHomeAppCountChange = {
                        homeAppCount = it
                        preferences.setHomeAppCount(it)
                    },
                    onShowTaglineChange = {
                        showTagline = it
                        preferences.setShowTagline(it)
                    },
                    onShowAppCountChange = {
                        showAppCount = it
                        preferences.setShowAppCount(it)
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
                    onShowQuoteChange = {
                        showQuote = it
                        preferences.setShowQuote(it)
                    },
                    onShowBatteryChange = {
                        showBattery = it
                        preferences.setShowBattery(it)
                    },
                    onAppTileContentModeChange = {
                        appTileContentMode = it
                        preferences.setAppTileContentMode(it)
                    },
                    onBrutalityLevelChange = {
                        brutalityLevel = it
                        preferences.setBrutalityLevel(it)
                    },
                    onClockStyleChange = {
                        clockStyle = it
                        preferences.setClockStyle(it)
                    },
                    onChooseWallpaper = {
                        wallpaperPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onClearWallpaper = {
                        wallpaperUri = null
                        preferences.setWallpaperUri(null)
                    },
                    onRefreshApps = { refreshApps() },
                    onClearFavorites = {
                        favorites = emptySet()
                        preferences.clearFavorites()
                    }
                )
            }
        }

        else -> {
            NeoBrutalTheme(
                themePreference = themePreference,
                brutalityLevel = brutalityLevel,
            ) {
                LauncherPageHost(
                    currentPage = currentPage,
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
                            showDate = showDate,
                            showTagline = showTagline,
                            showAppCount = showAppCount,
                            showWeather = showWeather,
                            showQuote = showQuote,
                            showBattery = showBattery,
                            appTileContentMode = appTileContentMode,
                            clockStyle = clockStyle,
                            wallpaperUri = wallpaperUri,
                            onOpenSettings = { settingsOpen = true },
                            onOpenApps = { currentPage = 1 },
                            onLaunch = repository::launch,
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
                            onToggleFavorite = { app ->
                                val key = app.packageName + "/" + app.activityName
                                val updated = favorites.toMutableSet()
                                if (updated.contains(key)) {
                                    updated.remove(key)
                                } else if (updated.size < 5) {
                                    updated.add(key)
                                }
                                favorites = updated
                                preferences.setFavorites(updated)
                            },
                            onLaunch = repository::launch,
                            onOpenHome = { currentPage = 0 }
                        )
                    } else {
                        LivePage(
                            apps = apps,
                            onLaunch = repository::launch,
                            onOpenHome = { currentPage = 0 }
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
    onPageChange: (Int) -> Unit,
    content: @Composable (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { 3 }
    )

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
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
                        .width(10.dp)
                        .height(10.dp)
                        .background(
                            if (pagerState.currentPage == index) BrutalColors.Orange else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = BrutalColors.Orange
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
                color = BrutalColors.Orange
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
    showDate: Boolean,
    showTagline: Boolean,
    showAppCount: Boolean,
    showWeather: Boolean,
    showQuote: Boolean,
    showBattery: Boolean,
    appTileContentMode: TileContentMode,
    clockStyle: ClockStyle,
    wallpaperUri: String?,
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
    val quoteRotation = rememberLiveTileData(
        tileId = "home-quotes",
        refreshIntervalMillis = 30L * 60L * 1000L,
        initialValue = (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    ) {
        (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    }.value ?: 0

    var weatherRefreshToken by remember { mutableIntStateOf(0) }
    var noteItems by remember { mutableStateOf(preferences.noteItems()) }
    var taskItems by remember { mutableStateOf(preferences.taskItems()) }
    var selectedTile by remember { mutableStateOf<NeoTileSpec?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }
    var excludedHomeApps by remember { mutableStateOf(preferences.excludedHomeApps()) }
    var appShortcutKey by remember { mutableStateOf(preferences.appShortcutKey()) }

    val timePattern = if (use24Hour) "HH:mm" else "hh:mm a"
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
    val palette = remember { BrutalColors.appPalette(0) }
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
                            text = "NEO / HOME",
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
                    background = MaterialTheme.colorScheme.surface,
                    borderWidth = 3.dp,
                    borderColor = MaterialTheme.colorScheme.onBackground,
                    shadowX = 5.dp,
                    shadowY = 5.dp
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize().padding(13.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = time.format(now),
                                fontSize = when (clockStyle) {
                                    ClockStyle.HUGE -> 72.sp
                                    ClockStyle.CONDENSED -> 60.sp
                                    ClockStyle.MONO -> 56.sp
                                    ClockStyle.POSTER -> 64.sp
                                },
                                lineHeight = when (clockStyle) {
                                    ClockStyle.HUGE -> 69.sp
                                    ClockStyle.CONDENSED -> 58.sp
                                    ClockStyle.MONO -> 54.sp
                                    ClockStyle.POSTER -> 61.sp
                                },
                                fontWeight = FontWeight.Black,
                                fontFamily = when (clockStyle) {
                                    ClockStyle.MONO -> BrutalTypography.Mono
                                    ClockStyle.CONDENSED -> BrutalTypography.Poster
                                    ClockStyle.HUGE -> BrutalTypography.Poster
                                    ClockStyle.POSTER -> BrutalTypography.Poster
                                },
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = longDay.format(now),
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = BrutalTypography.Bricolage,
                                    color = BrutalColors.Red,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                                Text(
                                    text = "/",
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = BrutalTypography.Bricolage,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = longDate.format(now),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BrutalTypography.Bricolage,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
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
                        Text(
                            text = "YOUR PHONE\nDOESN'T NEED\nTO LOOK CALM.",
                            fontSize = 7.sp,
                            lineHeight = 8.sp,
                            fontWeight = FontWeight.Black,
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
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f)
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
                        onTileLongPress = { tileEditMode = true },
                        onTileEdit = { selectedTile = it },
                        editMode = tileEditMode,
                        modifier = Modifier.fillMaxWidth(),
                        gap = 8.dp
                    )
                }
            }

            item(key = "notes-tasks") {
                NeoNotesTasksTile(
                    notes = noteItems,
                    tasks = taskItems,
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f),
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
                    },
                    onAddTask = { text ->
                        val updated = taskItems + NeoListItem(text = text)
                        taskItems = updated
                        preferences.setTaskItems(updated)
                    },
                    onEditTask = { index, text ->
                        val updated = taskItems.mapIndexed { itemIndex, item ->
                            if (itemIndex == index) item.copy(text = text) else item
                        }
                        taskItems = updated
                        preferences.setTaskItems(updated)
                    },
                    onToggleTask = { index ->
                        val updated = taskItems.mapIndexed { itemIndex, item ->
                            if (itemIndex == index) item.copy(checked = !item.checked) else item
                        }
                        taskItems = updated
                        preferences.setTaskItems(updated)
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
                    val quotePair = NeoQuotes.pairForRotation(quoteRotation)
                    NeoQuoteTilePlain(
                        quote = quotePair.first,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    NeoQuoteTilePlain(
                        quote = quotePair.second,
                        modifier = Modifier.weight(2f).aspectRatio(2f),
                        textColor = BrutalColors.Red
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
            title = { Text(text = tile.label + " / TILE", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (locked) {
                            "BOTTOM APP IS FIXED TO 4x1"
                        } else {
                            "SIZE: " + tile.size.label
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
                                    text = option.label,
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
                                text = "REMOVE",
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Orange
                            )
                        }
                    }

                    androidx.compose.material3.TextButton(
                        onClick = {
                            selectedTile = null
                            tileEditMode = false
                        }
                    ) {
                        Text(text = "CLOSE", fontWeight = FontWeight.Black)
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
    val iconBitmap = remember(app?.packageName, app?.activityName) {
        app?.icon?.toBitmap(96, 96)?.asImageBitmap()
    }

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
            if (app == null || iconBitmap == null) {
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
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(6.dp))
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
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "“" + quote + "”",
            fontSize = 13.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ClockTileContent(
    now: Date,
    time: SimpleDateFormat,
    date: SimpleDateFormat,
    use24Hour: Boolean,
    showDate: Boolean,
    style: ClockStyle,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.padding(9.dp)
    ) {
        val compact = minOf(maxWidth, maxHeight)

        when (style) {
            ClockStyle.POSTER -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = time.format(now),
                        fontSize = when {
                            compact < 78.dp -> 12.sp
                            compact < 155.dp -> 31.sp
                            else -> 58.sp
                        },
                        lineHeight = when {
                            compact < 78.dp -> 13.sp
                            compact < 155.dp -> 32.sp
                            else -> 59.sp
                        },
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                    if (showDate) {
                        Spacer(Modifier.height(if (compact < 155.dp) 3.dp else 5.dp))
                        Text(
                            text = date.format(now).uppercase(),
                            fontSize = when {
                                compact < 78.dp -> 6.sp
                                compact < 155.dp -> 9.sp
                                else -> 16.sp
                            },
                            lineHeight = when {
                                compact < 78.dp -> 7.sp
                                compact < 155.dp -> 10.sp
                                else -> 17.sp
                            },
                            fontWeight = FontWeight.Black,
                            letterSpacing = if (compact < 155.dp) 0.sp else 1.5.sp,
                            color = BrutalColors.Ink,
                            maxLines = 1
                        )
                    }
                }
            }

            ClockStyle.MONO -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = time.format(now),
                        fontFamily = BrutalTypography.Mono,
                        fontSize = when {
                            compact < 78.dp -> 11.sp
                            compact < 155.dp -> 28.sp
                            else -> 52.sp
                        },
                        lineHeight = when {
                            compact < 78.dp -> 12.sp
                            compact < 155.dp -> 29.sp
                            else -> 53.sp
                        },
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (compact < 155.dp) 0.5.sp else 1.sp,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                    if (showDate) {
                        Text(
                            text = "SYSTEM // ${date.format(now).uppercase()}",
                            fontFamily = BrutalTypography.Mono,
                            fontSize = if (compact < 155.dp) 7.sp else 10.sp,
                            lineHeight = if (compact < 155.dp) 8.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrutalColors.Ink,
                            maxLines = 1
                        )
                    }
                }
            }

            ClockStyle.CONDENSED -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = time.format(now),
                        fontSize = when {
                            compact < 78.dp -> 12.sp
                            compact < 155.dp -> 27.sp
                            else -> 48.sp
                        },
                        lineHeight = when {
                            compact < 78.dp -> 12.sp
                            compact < 155.dp -> 27.sp
                            else -> 47.sp
                        },
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                    if (showDate) {
                        Text(
                            text = date.format(now).uppercase(),
                            fontSize = when {
                                compact < 78.dp -> 6.sp
                                compact < 155.dp -> 8.sp
                                else -> 12.sp
                            },
                            lineHeight = when {
                                compact < 78.dp -> 7.sp
                                compact < 155.dp -> 9.sp
                                else -> 13.sp
                            },
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.2).sp,
                            color = BrutalColors.Ink,
                            maxLines = 1
                        )
                    }
                }
            }

            ClockStyle.HUGE -> {
                val hourFormat = remember(use24Hour) {
                    SimpleDateFormat(if (use24Hour) "HH" else "hh", Locale.getDefault())
                }
                val minuteFormat = remember {
                    SimpleDateFormat("mm", Locale.getDefault())
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = hourFormat.format(now),
                            fontSize = when {
                                compact < 78.dp -> 20.sp
                                compact < 155.dp -> 46.sp
                                else -> 82.sp
                            },
                            lineHeight = when {
                                compact < 78.dp -> 20.sp
                                compact < 155.dp -> 46.sp
                                else -> 82.sp
                            },
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = 1
                        )
                        Text(
                            text = minuteFormat.format(now),
                            fontSize = when {
                                compact < 78.dp -> 20.sp
                                compact < 155.dp -> 46.sp
                                else -> 82.sp
                            },
                            lineHeight = when {
                                compact < 78.dp -> 20.sp
                                compact < 155.dp -> 46.sp
                                else -> 82.sp
                            },
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = 1
                        )
                    }
                    if (showDate && compact >= 155.dp) {
                        Text(
                            text = date.format(now).uppercase(),
                            modifier = Modifier.weight(1f).padding(start = 10.dp),
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = BrutalColors.Ink,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NeoQuoteTile(
    quote: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val quoteBackground = if (isDark) BrutalColors.DarkTile else BrutalColors.Purple
    val quoteText = if (isDark) BrutalColors.DarkWhite else BrutalColors.White

    BrutalBlock(
        modifier = modifier,
        background = quoteBackground,
        borderWidth = 3.dp,
        shadowX = 6.dp,
        shadowY = 6.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(9.dp)
        ) {
            val compact = minOf(maxWidth, maxHeight)
            val headerSize = if (emphasized) 12.sp else {
                when {
                    compact < 78.dp -> 7.sp
                    compact < 155.dp -> 10.sp
                    else -> 11.sp
                }
            }

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
                    .coerceAtMost(if (emphasized) 6 else 6)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(if (emphasized) 6.dp else 5.dp)
            ) {
                Text(
                    text = "NEO QUOTE",
                    fontFamily = BrutalTypography.Display,
                    fontSize = headerSize,
                    lineHeight = headerSize,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = if (emphasized) 0.5.sp else 1.sp,
                    color = quoteText,
                    maxLines = 1
                )

                Text(
                    text = "“$quote”",
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = BrutalTypography.Body,
                    fontSize = quoteSize.sp,
                    lineHeight = (quoteSize * 1.08f).sp,
                    fontWeight = FontWeight.Bold,
                    color = quoteText,
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
        val isSmall = tileSize == NeoTileSize.SMALL
        val isHorizontal = tileSize == NeoTileSize.HORIZONTAL
        val isThreeByOne = tileSize == NeoTileSize.THREE_BY_ONE
        val isFourByOne = tileSize == NeoTileSize.FOUR_BY_ONE

        val iconBitmap = remember(app.packageName) {
            app.icon.toBitmap(64, 64).asImageBitmap()
        }

        val smallTextSize = when {
            app.label.length > 18 -> 7.sp
            app.label.length > 11 -> 8.sp
            else -> 9.sp
        }

        val horizontalTextSize = when {
            app.label.length > 18 -> 10.sp
            app.label.length > 12 -> 12.sp
            else -> 15.sp
        }

        val threeByOneTextSize = when {
            app.label.length > 20 -> 15.sp
            app.label.length > 14 -> 19.sp
            else -> 23.sp
        }

        val fourByOneTextSize = when {
            app.label.length > 22 -> 19.sp
            app.label.length > 15 -> 24.sp
            else -> 30.sp
        }

        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = background,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            when {
                isFourByOne -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = app.label.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            fontFamily = BrutalTypography.Display,
                            fontSize = fourByOneTextSize,
                            lineHeight = (fourByOneTextSize.value * 1.02f).sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                isHorizontal -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 9.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = app.label.uppercase(),
                            modifier = Modifier.weight(1f),
                            fontFamily = BrutalTypography.Display,
                            fontSize = horizontalTextSize,
                            lineHeight = (horizontalTextSize.value * 1.05f).sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                isThreeByOne -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = app.label.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            fontFamily = BrutalTypography.Display,
                            fontSize = threeByOneTextSize,
                            lineHeight = (threeByOneTextSize.value * 1.02f).sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                isSmall && contentMode == TileContentMode.ICON -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                isSmall && contentMode == TileContentMode.TEXT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.label.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = BrutalTypography.Display,
                            fontSize = smallTextSize,
                            lineHeight = (smallTextSize.value * 1.05f).sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                isSmall -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 5.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = app.label.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = BrutalTypography.Display,
                            fontSize = smallTextSize,
                            lineHeight = (smallTextSize.value * 1.05f).sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = when (variant % 4) {
                            0 -> Alignment.TopStart
                            1 -> Alignment.TopEnd
                            2 -> Alignment.BottomStart
                            else -> Alignment.Center
                        }
                    ) {
                        if (contentMode == TileContentMode.TEXT) {
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                fontFamily = BrutalTypography.Display,
                                fontSize = 18.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    appsCount: Int,
    themePreference: ThemePreference,
    use24Hour: Boolean,
    showDate: Boolean,
    homeAppCount: Int,
    showTagline: Boolean,
    showAppCount: Boolean,
    showWeather: Boolean,
    showQuote: Boolean,
    showBattery: Boolean,
    appTileContentMode: TileContentMode,
    brutalityLevel: BrutalityLevel,
    clockStyle: ClockStyle,
    wallpaperUri: String?,
    favoritesCount: Int,
    locationPermissionGranted: Boolean,
    notificationAccessGranted: Boolean,
    onBack: () -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onShowDateChange: (Boolean) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    onShowTaglineChange: (Boolean) -> Unit,
    onShowAppCountChange: (Boolean) -> Unit,
    onShowWeatherChange: (Boolean) -> Unit,
    onRequestWeatherPermission: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onShowQuoteChange: (Boolean) -> Unit,
    onShowBatteryChange: (Boolean) -> Unit,
    onAppTileContentModeChange: (TileContentMode) -> Unit,
    onBrutalityLevelChange: (BrutalityLevel) -> Unit,
    onClockStyleChange: (ClockStyle) -> Unit,
    onChooseWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    onRefreshApps: () -> Unit,
    onClearFavorites: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val uiBackground = MaterialTheme.colorScheme.background
    val uiSurface = MaterialTheme.colorScheme.surface
    val uiOnSurface = MaterialTheme.colorScheme.onSurface

    val darkTileBackground =
        if (uiBackground == BrutalColors.DarkPaper) {
            BrutalColors.DarkTile
        } else {
            BrutalColors.Ink
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(uiBackground)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = uiOnSurface
                )
            }

            BrutalBlock(
                modifier = Modifier.weight(1f),
                background = BrutalColors.Cyan,
                borderWidth = 3.dp,
                shadowX = 4.dp,
                shadowY = 4.dp
            ) {
                Column {
                    Text(
                        text = "NEO SETTINGS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = "CONTROL YOUR LAUNCHER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = BrutalColors.Ink
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSectionTitle("APPEARANCE")

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = uiSurface,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "APP THEME",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = uiOnSurface
                    )
                    Text(
                        text = "This theme changes the launcher interface only. The home clock keeps its own appearance.",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = uiOnSurface.copy(alpha = 0.75f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeButton(
                            label = "SYSTEM",
                            selected = themePreference == ThemePreference.SYSTEM,
                            background = BrutalColors.Cyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeChange(ThemePreference.SYSTEM) }
                        )
                        ThemeButton(
                            label = "LIGHT",
                            selected = themePreference == ThemePreference.LIGHT,
                            background = BrutalColors.Yellow,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeChange(ThemePreference.LIGHT) }
                        )
                        ThemeButton(
                            label = "DARK",
                            selected = themePreference == ThemePreference.DARK,
                            background = BrutalColors.Purple,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeChange(ThemePreference.DARK) }
                        )
                    }
                }
            }

            SettingsSectionTitle("PERSONALITY")

            BrutalSection(
                title = "BRUTALITY LEVEL",
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Pink
            ) {
                Text(
                    text = "Controls border and hard-shadow intensity across the launcher.",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BrutalityLevel.values().forEach { level ->
                        ThemeButton(
                            label = level.label,
                            selected = brutalityLevel == level,
                            background = when (level) {
                                BrutalityLevel.LITE -> BrutalColors.White
                                BrutalityLevel.BRUTAL -> BrutalColors.Yellow
                                BrutalityLevel.HARD -> BrutalColors.Orange
                                BrutalityLevel.CHAOS -> BrutalColors.Purple
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { onBrutalityLevelChange(level) }
                        )
                    }
                }
            }

            SettingsSectionTitle("CLOCK")


            BrutalSection(
                title = "TYPOGRAPHY STYLE",
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Purple
            ) {
                Text(
                    text = "Choose the visual personality of the home clock.",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ClockStyle.values().forEach { style ->
                        ThemeButton(
                            label = style.label,
                            selected = clockStyle == style,
                            background = when (style) {
                                ClockStyle.POSTER -> BrutalColors.Yellow
                                ClockStyle.MONO -> BrutalColors.Cyan
                                ClockStyle.CONDENSED -> BrutalColors.Lime
                                ClockStyle.HUGE -> BrutalColors.Pink
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { onClockStyleChange(style) }
                        )
                    }
                }
            }

            SettingsSwitch(
                title = "24-HOUR FORMAT",
                description = "Use 24-hour time instead of AM/PM.",
                checked = use24Hour,
                background = BrutalColors.Yellow,
                onCheckedChange = onUse24HourChange
            )

                    SettingsSectionTitle("PERMISSIONS")

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = uiSurface,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "WEATHER LOCATION",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = uiOnSurface
                    )
                    Text(
                        text = if (locationPermissionGranted) {
                            "LOCATION ACCESS GRANTED. WEATHER CAN USE YOUR CURRENT AREA."
                        } else {
                            "LOCATION ACCESS IS NEEDED TO SHOW LOCAL WEATHER."
                        },
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = uiOnSurface.copy(alpha = 0.75f)
                    )
                    BrutalActionButton(
                        title = if (locationPermissionGranted) "LOCATION ALREADY ALLOWED" else "ALLOW WEATHER LOCATION",
                        background = if (locationPermissionGranted) BrutalColors.Lime else BrutalColors.Orange,
                        onClick = onRequestWeatherPermission
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "MUSIC + NOTIFICATION ACCESS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = uiOnSurface
                    )
                    Text(
                        text = if (notificationAccessGranted) {
                            "NOTIFICATION ACCESS GRANTED. MUSIC TILE CAN READ ACTIVE MEDIA SESSIONS. THIS ACCESS CAN ALSO EXPOSE OTHER NOTIFICATIONS, INCLUDING MESSAGE NOTIFICATIONS; NEO CURRENTLY USES IT ONLY FOR MUSIC."
                        } else {
                            "ENABLE ANDROID NOTIFICATION ACCESS FOR THE MUSIC TILE TO DETECT THE MUSIC APP AND CURRENT TRACK."
                        },
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = uiOnSurface.copy(alpha = 0.75f)
                    )
                    BrutalActionButton(
                        title = if (notificationAccessGranted) "OPEN NOTIFICATION ACCESS" else "ALLOW MUSIC / NOTIFICATION ACCESS",
                        background = if (notificationAccessGranted) BrutalColors.Cyan else BrutalColors.Orange,
                        onClick = onOpenNotificationAccess
                    )
                }
            }

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Lime,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "HOME APP COUNT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Show $homeAppCount launchable apps on the home screen.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (count in 2..8) {
                            ThemeButton(
                                label = count.toString(),
                                selected = homeAppCount == count,
                                background = when (count) {
                                    3 -> BrutalColors.Pink
                                    5 -> BrutalColors.Orange
                                    else -> BrutalColors.Cyan
                                },
                                modifier = Modifier.weight(1f),
                                onClick = { onHomeAppCountChange(count) }
                            )
                        }
                    }
                }
            }

            SettingsSectionTitle("WALLPAPER")

            BrutalSection(
                title = "HOME BACKGROUND",
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Yellow
            ) {
                Text(
                    text = if (wallpaperUri == null) {
                        "PAPER / SYSTEM THEME BACKGROUND"
                    } else {
                        "CUSTOM IMAGE SELECTED"
                    },
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrutalActionButton(
                        title = "CHOOSE IMAGE",
                        background = BrutalColors.Cyan,
                        modifier = Modifier.weight(1f),
                        onClick = onChooseWallpaper
                    )
                    BrutalActionButton(
                        title = "CLEAR",
                        background = BrutalColors.Pink,
                        modifier = Modifier.weight(1f),
                        onClick = onClearWallpaper
                    )
                }
            }

            SettingsSectionTitle("APP TILES")


            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Lime,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "APP TILE CONTENT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Choose one layout for every app tile on the home screen.",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            TileContentMode.ICON,
                            TileContentMode.ICON_TEXT,
                            TileContentMode.TEXT
                        ).forEach { mode ->
                            ThemeButton(
                                label = when (mode) {
                                    TileContentMode.ICON -> "ICON"
                                    TileContentMode.ICON_TEXT -> "ICON + TEXT"
                                    TileContentMode.TEXT -> "TEXT"
                                },
                                selected = appTileContentMode == mode,
                                background = when (mode) {
                                    TileContentMode.ICON -> BrutalColors.Cyan
                                    TileContentMode.ICON_TEXT -> BrutalColors.Yellow
                                    TileContentMode.TEXT -> BrutalColors.Pink
                                },
                                modifier = Modifier.weight(1f),
                                onClick = { onAppTileContentModeChange(mode) }
                            )
                        }
                    }
                }
            }

            SettingsSectionTitle("FAVORITES")

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = uiSurface,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "$favoritesCount PINNED APPS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = uiOnSurface
                    )
                    Text(
                        text = "Long-press any app on the APPS page to pin or unpin it. Pinned apps appear first on the home screen.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = uiOnSurface
                    )
                    BrutalActionButton(
                        title = "CLEAR ALL PINNED APPS",
                        background = BrutalColors.Orange,
                        onClick = onClearFavorites
                    )
                }
            }

            SettingsSectionTitle("APP LIST")

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = uiSurface,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "$appsCount LAUNCHABLE APPS DETECTED",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = uiOnSurface
                    )
                    Text(
                        text = "Refresh the launcher app list after installing or removing apps.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = uiOnSurface
                    )
                    BrutalActionButton(
                        title = "REFRESH APP LIST",
                        background = BrutalColors.Orange,
                        onClick = onRefreshApps
                    )
                }
            }

            SettingsSectionTitle("LAUNCHER")

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Pink,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = BrutalColors.Ink,
                            modifier = Modifier
                                .width(26.dp)
                                .height(26.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "DEFAULT HOME APP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "Open Android's Home app settings to choose Neo Brutal Launcher as the default launcher.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    BrutalActionButton(
                        title = "OPEN HOME SETTINGS",
                        background = BrutalColors.Yellow,
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                    )
                }
            }

            SettingsSectionTitle("ABOUT")

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = darkTileBackground,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "NEO BRUTAL LAUNCHER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.White
                    )
                    Text(
                        text = "CORE BUILD 0.1.0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = BrutalColors.Cyan
                    )
                    Text(
                        text = "A neo-brutalist launcher focused on fast access to your apps.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalColors.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
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
            color = MaterialTheme.colorScheme.onSurface
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
