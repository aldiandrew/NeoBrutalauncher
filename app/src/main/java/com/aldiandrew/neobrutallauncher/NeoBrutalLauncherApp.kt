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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var cornerRadius by remember { mutableStateOf(preferences.cornerRadius()) }
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
                    cornerRadius = cornerRadius,
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
                    onCornerRadiusChange = {
                        cornerRadius = it
                        preferences.setCornerRadius(it)
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
                ) {
                    if (currentPage == 0) {
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
                                if (!updated.add(key)) {
                                    updated.remove(key)
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
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(currentPage) {
                var totalX = 0f
                var totalY = 0f
                detectDragGestures(
                    onDragStart = {
                        totalX = 0f
                        totalY = 0f
                    },
                    onDragCancel = {
                        totalX = 0f
                        totalY = 0f
                    },
                    onDragEnd = {
                        val horizontal = abs(totalX) > abs(totalY)
                        if (horizontal && abs(totalX) >= 96f) {
                            when {
                                totalX < 0f && currentPage == 0 -> onPageChange(1)
                                totalX > 0f && currentPage == 1 -> onPageChange(0)
                            }
                        }
                        totalX = 0f
                        totalY = 0f
                    }
                ) { change, amount ->
                    totalX += amount.x
                    totalY += amount.y

                    // Only consume horizontal gestures. Vertical tile drags remain owned
                    // by the tile gesture handler.
                    if (abs(totalX) > abs(totalY) && abs(totalX) > 8f) {
                        change.consume()
                    }
                }
            }
    ) {
        content()
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
    onLaunch: (AppInfo) -> Unit,
    tilePositions: Map<String, NeoTilePosition>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    tileSizes: Map<String, NeoTileSize>,
    onTileSizeChange: (String, NeoTileSize) -> Unit,
) {
    BackHandler(onBack = {})

    val context = androidx.compose.ui.platform.LocalContext.current
    val darkTileBackground =
        if (MaterialTheme.colorScheme.background == BrutalColors.DarkPaper) {
            BrutalColors.DarkTile
        } else {
            BrutalColors.Ink
        }

    val now = rememberMinuteClock()

    var selectedTile by remember { mutableStateOf<NeoTileSpec?>(null) }
    var weatherRefreshToken by remember { mutableIntStateOf(0) }

    val timePattern = if (use24Hour) "HH:mm" else "hh:mm a"

    val time = remember(timePattern) {
        SimpleDateFormat(timePattern, Locale.getDefault())
    }

    val date = remember {
        SimpleDateFormat("EEE / dd MMM", Locale.getDefault())
    }

    val favoriteApps = apps.filter {
        favorites.contains(it.packageName + "/" + it.activityName)
    }
    val remainingApps = apps.filterNot {
        favorites.contains(it.packageName + "/" + it.activityName)
    }
    val topApps = (favoriteApps + remainingApps).take(homeAppCount)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (wallpaperUri == null) MaterialTheme.colorScheme.background
                else Color.Transparent
            )
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        BrutalWallpaper(
            uriString = wallpaperUri,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

                BrutalBlock(
                    modifier = Modifier
                        .width(90.dp)
                        .clickable(onClick = onOpenSettings),
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
                            modifier = Modifier
                                .width(18.dp)
                                .height(18.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "V0.1",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink
                        )
                    }
                }
            }

            NeoTileGrid(
                tiles = buildList {
                    add(
                        NeoTileSpec(
                            id = "clock",
                            size = tileSizes["clock"] ?: NeoTileSize.WIDE,
                            label = "CLOCK"
                        ) {
                            BrutalBlock(
                                modifier = Modifier.fillMaxSize(),
                                background = BrutalColors.Yellow,
                                borderWidth = 4.dp,
                                shadowX = 8.dp,
                                shadowY = 8.dp
                            ) {
                                ClockTileContent(
                                    now = now,
                                    time = time,
                                    date = date,
                                    use24Hour = use24Hour,
                                    showDate = showDate,
                                    style = clockStyle,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    )

                    if (showWeather) {
                        add(
                            NeoTileSpec(
                                id = "weather",
                                size = tileSizes["weather"] ?: NeoTileSize.WIDE,
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

                    if (showQuote) {
                        add(
                            NeoTileSpec(
                                id = "quote",
                                size = tileSizes["quote"] ?: NeoTileSize.WIDE,
                                label = "QUOTE"
                            ) {
                                NeoQuoteTile(
                                    quote = NeoQuotes.forToday(),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )
                    }

                    if (showTagline) {
                        add(
                            NeoTileSpec(
                                id = "tagline",
                                size = tileSizes["tagline"] ?: NeoTileSize.WIDE,
                                label = "TAGLINE"
                            ) {
                                BrutalBlock(
                                    modifier = Modifier.fillMaxSize(),
                                    background = BrutalColors.Purple,
                                    borderWidth = 3.dp,
                                    shadowX = 5.dp,
                                    shadowY = 5.dp
                                ) {
                                    BoxWithConstraints(
                                        modifier = Modifier.fillMaxSize().padding(9.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        val compact = minOf(maxWidth, maxHeight)
                                        Text(
                                            text = "YOUR PHONE DOESN'T NEED TO LOOK CALM.",
                                            fontSize = when {
                                                compact < 78.dp -> 8.sp
                                                compact < 155.dp -> 14.sp
                                                else -> 21.sp
                                            },
                                            lineHeight = when {
                                                compact < 78.dp -> 9.sp
                                                compact < 155.dp -> 15.sp
                                                else -> 23.sp
                                            },
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.White,
                                            maxLines = if (compact < 78.dp) 5 else 4
                                        )
                                    }
                                }
                            }
                        )
                    }

                    topApps.forEachIndexed { index, app ->
                        val tilePalette = BrutalColors.appPalette(chaosSeed)
                        val tileColor = tilePalette[index % tilePalette.size]

                        add(
                            NeoTileSpec(
                                id = "app_" + app.packageName + "_" + app.activityName,
                                size = tileSizes["app_" + app.packageName + "_" + app.activityName]
                                    ?: NeoTileSize.MEDIUM,
                                label = app.label.uppercase(),
                                onClick = { onLaunch(app) }
                            ) {
                                AppTile(
                                    app = app,
                                    background = tileColor,
                                    modifier = Modifier.fillMaxSize(),
                                    contentMode = appTileContentMode
                                )
                            }
                        )
                    }

                    if (topApps.isEmpty()) {
                        add(
                            NeoTileSpec(
                                id = "empty-apps",
                                size = tileSizes["empty-apps"] ?: NeoTileSize.WIDE,
                                label = "APPS"
                            ) {
                                BrutalBlock(
                                    modifier = Modifier.fillMaxSize(),
                                    background = BrutalColors.White,
                                    borderWidth = 3.dp,
                                    shadowX = 6.dp,
                                    shadowY = 6.dp
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "NO LAUNCHABLE APPS DETECTED",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.Ink
                                        )
                                    }
                                }
                            }
                        )
                    }

                    if (showBattery) {
                        add(
                            NeoTileSpec(
                                id = "battery",
                                size = tileSizes["battery"] ?: NeoTileSize.MEDIUM,
                                label = "BATTERY"
                            ) {
                                BatteryTile(
                                    context = context,
                                    modifier = Modifier.fillMaxSize(),
                                    background = BrutalColors.Orange
                                )
                            }
                        )
                    }

                    if (showAppCount) {
                        add(
                            NeoTileSpec(
                                id = "system",
                                size = tileSizes["system"] ?: NeoTileSize.WIDE,
                                label = "SYSTEM"
                            ) {
                                BrutalBlock(
                                    modifier = Modifier.fillMaxSize(),
                                    background = BrutalColors.White,
                                    borderWidth = 3.dp,
                                    shadowX = 6.dp,
                                    shadowY = 6.dp
                                ) {
                                    BoxWithConstraints(
                                        modifier = Modifier.fillMaxSize().padding(9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val compact = minOf(maxWidth, maxHeight)
                                        if (compact < 78.dp) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = apps.size.toString(),
                                                    fontSize = 20.sp,
                                                    lineHeight = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = BrutalColors.Ink
                                                )
                                                Text(
                                                    text = "APPS",
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = BrutalColors.Ink
                                                )
                                            }
                                        } else {
                                            Column(
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "SYSTEM",
                                                    fontSize = if (compact < 155.dp) 11.sp else 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.sp,
                                                    color = BrutalColors.Ink
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = apps.size.toString() + " APPS DETECTED",
                                                    fontSize = if (compact < 155.dp) 19.sp else 25.sp,
                                                    lineHeight = if (compact < 155.dp) 20.sp else 26.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = BrutalColors.Ink,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                },
                positions = tilePositions,
                onPositionsChange = onTilePositionsChange,
                onTileLongPress = { selectedTile = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
        }
    }

    selectedTile?.let { tile ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedTile = null },
            title = {
                Text(
                    text = tile.label + " / TILE",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SIZE: " + tile.size.label,
                        fontWeight = FontWeight.Black
                    )
                    listOf(
                        NeoTileSize.SMALL,
                        NeoTileSize.MEDIUM,
                        NeoTileSize.WIDE,
                        NeoTileSize.LARGE
                    ).forEach { option ->
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onTileSizeChange(tile.id, option)
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
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { selectedTile = null }
                ) {
                    Text(
                        text = "CLOSE",
                        fontWeight = FontWeight.Black
                    )
                }
            }
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
    contentMode: TileContentMode
) {
    BoxWithConstraints(modifier = modifier) {
        val compactSize = minOf(maxWidth, maxHeight)
        val iconSize = (compactSize * 0.52f).coerceIn(38.dp, 78.dp)

        val baseTextSize = when {
            compactSize < 100.dp -> 10f
            compactSize < 180.dp -> 15f
            else -> 22f
        }

        val textScale = when {
            app.label.length > 24 -> 0.58f
            app.label.length > 18 -> 0.68f
            app.label.length > 12 -> 0.82f
            else -> 1f
        }

        val textSize = (baseTextSize * textScale).coerceAtLeast(9f).sp
        val iconBitmap = remember(app.packageName) {
            app.icon.toBitmap(96, 96).asImageBitmap()
        }

        BrutalBlock(
            modifier = Modifier.fillMaxSize(),
            background = background,
            borderWidth = 3.dp,
            shadowX = 5.dp,
            shadowY = 5.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                when (contentMode) {
                    TileContentMode.ICON -> {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                        )
                    }

                    TileContentMode.ICON_TEXT -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                bitmap = iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .width(iconSize)
                                    .height(iconSize)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = textSize,
                                lineHeight = textSize,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }

                    TileContentMode.TEXT -> {
                        Text(
                            text = app.label.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = textSize,
                            lineHeight = textSize,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink,
                            textAlign = TextAlign.Center,
                            maxLines = 3
                        )
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

            BrutalSection(
                title = "SHAPE",
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Cyan
            ) {
                Text(
                    text = "Optional corner radius. 0 keeps the original sharp Neo Brutal look.",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 4, 8, 16).forEach { radius ->
                        ThemeButton(
                            label = "\${radius}DP",
                            selected = cornerRadius == radius,
                            background = BrutalColors.White,
                            modifier = Modifier.weight(1f),
                            onClick = { onCornerRadiusChange(radius) }
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

            SettingsSwitch(
                title = "SHOW DATE",
                description = "Show the date below the home clock.",
                checked = showDate,
                background = BrutalColors.Cyan,
                onCheckedChange = onShowDateChange
            )

            SettingsSectionTitle("HOME")

            SettingsSwitch(
                title = "SHOW TAGLINE",
                description = "Show the neo-brutalist message below the clock.",
                checked = showTagline,
                background = BrutalColors.Pink,
                onCheckedChange = onShowTaglineChange
            )

            SettingsSwitch(
                title = "SHOW APP COUNT",
                description = "Show the detected-app count card on the home screen.",
                checked = showAppCount,
                background = BrutalColors.Cyan,
                onCheckedChange = onShowAppCountChange
            )

            SettingsSwitch(
                title = "SHOW WEATHER",
                description = "Show the standalone WEATHER tile with temperature, humidity and wind. Location and Internet access are required.",
                checked = showWeather,
                background = BrutalColors.Yellow,
                onCheckedChange = onShowWeatherChange
            )

            if (showWeather && !locationPermissionGranted) {
                BrutalActionButton(
                    title = "ALLOW LOCATION FOR WEATHER",
                    background = BrutalColors.Orange,
                    onClick = onRequestWeatherPermission
                )
            }

            SettingsSwitch(
                title = "SHOW QUOTE",
                description = "Show an original neo-brutalist quote tile on the home screen.",
                checked = showQuote,
                background = BrutalColors.Lime,
                onCheckedChange = onShowQuoteChange
            )

            SettingsSwitch(
                title = "SHOW BATTERY",
                description = "Show an event-driven battery tile. It updates only when Android reports a battery change.",
                checked = showBattery,
                background = BrutalColors.Orange,
                onCheckedChange = onShowBatteryChange
            )

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
                        for (count in listOf(2, 4, 6, 8)) {
                            ThemeButton(
                                label = count.toString(),
                                selected = homeAppCount == count,
                                background = when (count) {
                                    2 -> BrutalColors.Pink
                                    4 -> BrutalColors.Orange
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
    onClick: () -> Unit
) {
    BrutalBlock(
        modifier = Modifier
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
