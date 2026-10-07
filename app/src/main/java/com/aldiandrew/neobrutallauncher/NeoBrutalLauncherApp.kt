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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun NeoBrutalLauncherApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AppRepository(context) }
    val preferences = remember { LauncherPreferences(context) }

    var apps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var currentPage by remember { mutableStateOf(0) }
    var settingsOpen by remember { mutableStateOf(false) }

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
    var chaosSeed by remember { mutableStateOf(preferences.chaosSeed()) }

    var locationPermissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
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

    fun refreshApps() {
        apps = repository.loadApps()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshApps()
                locationPermissionGranted =
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
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
                cornerRadius = cornerRadius.dp
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
                    onChaosPalette = {
                        chaosSeed = (chaosSeed + 1).coerceAtLeast(1)
                        preferences.setChaosSeed(chaosSeed)
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
                cornerRadius = cornerRadius.dp
            ) {
                LauncherPageHost(
                    currentPage = currentPage,
                    onPageChange = {
                        currentPage = it.coerceIn(0, 1)
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
                            chaosSeed = chaosSeed,
                            onOpenSettings = { settingsOpen = true },
                            onOpenApps = { currentPage = 1 },
                            onLaunch = repository::launch,
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
                    } else {
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
        pageCount = { 2 }
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
            beyondViewportPageCount = 1,
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
            repeat(2) { index ->
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
                text = if (pagerState.currentPage == 0) "HOME" else "APPS",
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
    chaosSeed: Int,
    onOpenSettings: () -> Unit,
    onOpenApps: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    tilePositions: Map<String, NeoTilePosition>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    tileSizes: Map<String, NeoTileSize>,
    onTileSizeChange: (String, NeoTileSize) -> Unit,
) {
    BackHandler(onBack = {})

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { LauncherPreferences(context) }
    val now = rememberMinuteClock()

    var weatherRefreshToken by remember { mutableIntStateOf(0) }
    var noteItems by remember { mutableStateOf(preferences.noteItems()) }
    var taskItems by remember { mutableStateOf(preferences.taskItems()) }
    var selectedTile by remember { mutableStateOf<NeoTileSpec?>(null) }

    val timePattern = if (use24Hour) "HH:mm" else "hh:mm a"
    val time = remember(timePattern) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val longDay = remember { SimpleDateFormat("EEEE", Locale.ENGLISH) }
    val longDate = remember { SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH) }

    val launchCounts = preferences.appLaunchCounts()
    val rankedApps = apps.sortedWith(
        compareByDescending<AppInfo> { launchCounts[it.packageName + "/" + it.activityName] ?: 0 }
            .thenByDescending { favorites.contains(it.packageName + "/" + it.activityName) }
            .thenBy { it.label.lowercase() }
    )
    val launchableApps = rankedApps.take(homeAppCount.coerceIn(2, 8))
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = BrutalColors.Ink
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(82.dp)
                            .clickable(onClick = onOpenSettings)
                    ) {
                        BrutalBlock(
                            modifier = Modifier.fillMaxWidth(),
                            background = BrutalColors.Pink,
                            borderWidth = 3.dp,
                            shadowX = 4.dp,
                            shadowY = 4.dp
                        ) {
                            Text(
                                text = "V0.1",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink
                            )
                        }
                    }
                }
            }

            item(key = "clock") {
                BrutalBlock(
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                    background = BrutalColors.Yellow,
                    borderWidth = 4.dp,
                    shadowX = 8.dp,
                    shadowY = 8.dp
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
                                color = BrutalColors.Ink,
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
                                    color = BrutalColors.Purple,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                                Text(
                                    text = "/",
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = BrutalTypography.Bricolage,
                                    color = BrutalColors.Ink
                                )
                                Text(
                                    text = longDate.format(now),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BrutalTypography.Bricolage,
                                    color = BrutalColors.Ink,
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
                                    index == launchableApps.lastIndex -> NeoTileSize.WIDE
                                    index < 2 -> NeoTileSize.MEDIUM
                                    else -> NeoTileSize.SMALL
                                }
                                val size = if (index == launchableApps.lastIndex) {
                                    NeoTileSize.WIDE
                                } else {
                                    tileSizes[id] ?: defaultSize
                                }
                                val palette = BrutalColors.appPalette(chaosSeed)
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
                                            variant = index
                                        )
                                    }
                                )
                            }
                        },
                        positions = tilePositions.filterKeys { appTileIds.contains(it) },
                        onPositionsChange = onTilePositionsChange,
                        onTileLongPress = { selectedTile = it },
                        modifier = Modifier.fillMaxWidth(),
                        gap = 8.dp
                    )
                }
            }

            item(key = "notes") {
                NeoNoteTaskTile(
                    kind = NoteTaskKind.NOTES,
                    items = noteItems,
                    modifier = Modifier.fillMaxWidth().aspectRatio(4.8f),
                    onAddItem = { text ->
                        noteItems = noteItems + NeoListItem(text = text)
                        preferences.setNoteItems(noteItems)
                    },
                    onItemTextChange = { index, text ->
                        noteItems = noteItems.mapIndexed { itemIndex, item ->
                            if (itemIndex == index) item.copy(text = text) else item
                        }
                        preferences.setNoteItems(noteItems)
                    }
                )
            }

            item(key = "tasks") {
                NeoNoteTaskTile(
                    kind = NoteTaskKind.TASKS,
                    items = taskItems,
                    modifier = Modifier.fillMaxWidth().aspectRatio(4.8f),
                    onAddItem = { text ->
                        taskItems = taskItems + NeoListItem(text = text)
                        preferences.setTaskItems(taskItems)
                    },
                    onItemTextChange = { index, text ->
                        taskItems = taskItems.mapIndexed { itemIndex, item ->
                            if (itemIndex == index) item.copy(text = text) else item
                        }
                        preferences.setTaskItems(taskItems)
                    },
                    onToggleItem = { index ->
                        taskItems = taskItems.mapIndexed { itemIndex, item ->
                            if (itemIndex == index) item.copy(checked = !item.checked) else item
                        }
                        preferences.setTaskItems(taskItems)
                    }
                )
            }

            item(key = "footer") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoAddAppTile(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        onClick = onOpenApps
                    )
                    val quotePair = NeoQuotes.pairForToday()
                    NeoQuoteTilePlain(
                        quote = quotePair.first,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    NeoQuoteTilePlain(
                        quote = quotePair.second,
                        modifier = Modifier.weight(2f).aspectRatio(2f)
                    )
                }
            }
        }
    }

    selectedTile?.let { tile ->
        val lastId = launchableApps.lastOrNull()?.let {
            "app_" + it.packageName + "_" + it.activityName
        }
        val locked = tile.id == lastId

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedTile = null },
            title = { Text(text = tile.label + " / TILE", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (locked) {
                            "BOTTOM APP IS FIXED TO WIDE 4x2"
                        } else {
                            "SIZE: " + tile.size.label
                        },
                        fontWeight = FontWeight.Black
                    )
                    if (!locked) {
                        listOf(
                            NeoTileSize.SMALL,
                            NeoTileSize.MEDIUM,
                            NeoTileSize.TALL,
                            NeoTileSize.WIDE
                        ).forEach { option ->
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    onTileSizeChange(tile.id, option)
                                    selectedTile = null
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
                androidx.compose.material3.TextButton(onClick = { selectedTile = null }) {
                    Text(text = "CLOSE", fontWeight = FontWeight.Black)
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
    modifier: Modifier,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    Box(modifier = modifier.clickable(onClick = onClick)) {
        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = Color.Transparent,
            borderWidth = 3.dp,
            borderColor = textColor,
            shadowX = 0.dp,
            shadowY = 0.dp
        ) {
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
                    text = "ADD",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun NeoQuoteTilePlain(
    quote: String,
    modifier: Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
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
private fun NeoQuoteTile(
    quote: String,
    modifier: Modifier = Modifier
) {
    BrutalBlock(
        modifier = modifier,
        background = BrutalColors.Purple,
        borderWidth = 3.dp,
        shadowX = 6.dp,
        shadowY = 6.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(9.dp)
        ) {
            val compact = minOf(maxWidth, maxHeight)
            Column(
                verticalArrangement = Arrangement.spacedBy(if (compact < 100.dp) 3.dp else 5.dp)
            ) {
                Text(
                    text = "NEO QUOTE",
                    fontSize = if (compact < 78.dp) 7.sp else if (compact < 155.dp) 10.sp else 11.sp,
                    lineHeight = if (compact < 78.dp) 8.sp else 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (compact < 155.dp) 0.sp else 1.sp,
                    color = BrutalColors.White,
                    maxLines = 1
                )

                Text(
                    text = "“$quote”",
                    fontSize = when {
                        compact < 78.dp -> 7.sp
                        compact < 155.dp -> 12.sp
                        else -> 17.sp
                    },
                    lineHeight = when {
                        compact < 78.dp -> 8.sp
                        compact < 155.dp -> 14.sp
                        else -> 21.sp
                    },
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.White,
                    maxLines = when {
                        compact < 78.dp -> 5
                        compact < 155.dp -> 5
                        else -> 6
                    }
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
    variant: Int
) {
    BoxWithConstraints(modifier = modifier) {
        val compact = minOf(maxWidth, maxHeight)
        val isWide = maxWidth > maxHeight * 1.55f
        val iconSize = when {
            compact < 85.dp -> 30.dp
            compact < 145.dp -> 44.dp
            else -> 62.dp
        }
        val baseText = when {
            compact < 85.dp -> 8.5f
            compact < 145.dp -> 12.5f
            else -> 18f
        }
        val scale = when {
            app.label.length > 24 -> 0.58f
            app.label.length > 18 -> 0.68f
            app.label.length > 12 -> 0.82f
            else -> 1f
        }
        val textSize = (baseText * scale).coerceAtLeast(8f).sp
        val iconBitmap = remember(app.packageName) {
            app.icon.toBitmap(96, 96).asImageBitmap()
        }

        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = background,
            borderWidth = 3.dp,
            shadowX = 0.dp,
            shadowY = 0.dp
        ) {
            when (contentMode) {
                TileContentMode.ICON -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(iconSize)
                                .align(
                                    when (variant % 4) {
                                        0 -> Alignment.TopStart
                                        1 -> Alignment.TopEnd
                                        2 -> Alignment.BottomStart
                                        else -> Alignment.Center
                                    }
                                )
                        )
                    }
                }

                TileContentMode.TEXT -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(9.dp),
                        contentAlignment = when (variant % 4) {
                            0 -> Alignment.TopStart
                            1 -> Alignment.CenterEnd
                            2 -> Alignment.BottomStart
                            else -> Alignment.BottomEnd
                        }
                    ) {
                        Text(
                            text = app.label.uppercase(),
                            fontSize = textSize,
                            lineHeight = (textSize.value * 1.02f).sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            maxLines = if (compact < 100.dp) 2 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                TileContentMode.ICON_TEXT -> {
                    Box(modifier = Modifier.fillMaxSize().padding(9.dp)) {
                        if (isWide || variant % 3 == 0) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = iconBitmap,
                                    contentDescription = app.label,
                                    modifier = Modifier.size(iconSize)
                                )
                                Text(
                                    text = app.label.uppercase(),
                                    modifier = Modifier.weight(1f),
                                    fontSize = textSize,
                                    lineHeight = (textSize.value * 1.02f).sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(iconSize)
                                    .align(
                                        if (variant % 2 == 0) Alignment.TopStart else Alignment.TopEnd
                                    )
                            )
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.align(
                                    if (variant % 2 == 0) Alignment.BottomEnd else Alignment.BottomStart
                                ),
                                fontSize = textSize,
                                lineHeight = (textSize.value * 1.02f).sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = if (compact < 100.dp) 2 else 3,
                                overflow = TextOverflow.Ellipsis
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
    cornerRadius: Int,
    clockStyle: ClockStyle,
    wallpaperUri: String?,
    favoritesCount: Int,
    locationPermissionGranted: Boolean,
    onBack: () -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onShowDateChange: (Boolean) -> Unit,
    onHomeAppCountChange: (Int) -> Unit,
    onShowTaglineChange: (Boolean) -> Unit,
    onShowAppCountChange: (Boolean) -> Unit,
    onShowWeatherChange: (Boolean) -> Unit,
    onRequestWeatherPermission: () -> Unit,
    onShowQuoteChange: (Boolean) -> Unit,
    onShowBatteryChange: (Boolean) -> Unit,
    onAppTileContentModeChange: (TileContentMode) -> Unit,
    onBrutalityLevelChange: (BrutalityLevel) -> Unit,
    onCornerRadiusChange: (Int) -> Unit,
    onClockStyleChange: (ClockStyle) -> Unit,
    onChooseWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    onChaosPalette: () -> Unit,
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

            SettingsSectionTitle("HOME")

            BrutalSection(
                title = "FIXED HOME TILES",
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Cyan
            ) {
                Text(
                    text = "TAGLINE, WEATHER, BATTERY, NETWORK, MUSIC, NOTES, TASKS AND QUOTES ARE PART OF THE FIXED HOME STRUCTURE.",
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Black
                )
                if (!locationPermissionGranted) {
                    Spacer(Modifier.height(8.dp))
                    BrutalActionButton(
                        title = "ALLOW LOCATION FOR WEATHER",
                        background = BrutalColors.Orange,
                        onClick = onRequestWeatherPermission
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

            BrutalSection(
                title = "CHAOS PALETTE",
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Lime
            ) {
                Text(
                    text = "Reroll the app-tile palette while preserving the workspace structure.",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                BrutalActionButton(
                    title = "REROLL PALETTE",
                    background = BrutalColors.Orange,
                    onClick = onChaosPalette
                )
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
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BrutalActionButton(
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
