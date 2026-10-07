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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    var tileContentModes by remember { mutableStateOf(preferences.tileContentModes()) }

    var locationPermissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
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
            NeoBrutalTheme(themePreference = themePreference) {
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
                    onRefreshApps = { refreshApps() },
                    onClearFavorites = {
                        favorites = emptySet()
                        preferences.clearFavorites()
                    }
                )
            }
        }

        else -> {
            NeoBrutalTheme(themePreference = themePreference) {
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
                            onOpenApps = { currentPage = 1; refreshApps() },
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
                            tileContentModes = tileContentModes,
                            onTileContentModeChange = { tileId, mode ->
                                tileContentModes = tileContentModes + (tileId to mode)
                                preferences.setTileContentModes(tileContentModes)
                            }
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
                            onOpenHome = { currentPage = 0 },
                            onOpenSettings = { settingsOpen = true },
                            onRefresh = { refreshApps() }
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
    var accumulatedX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 62.dp)
        ) {
            androidx.compose.animation.Crossfade(
                targetState = currentPage,
                label = "launcherPage"
            ) {
                content()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(62.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("HOME", "APPS").forEachIndexed { index, label ->
                BrutalBlock(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPageChange(index) },
                    background = when {
                        index == currentPage -> when (index) {
                            0 -> BrutalColors.Yellow
                            else -> BrutalColors.Cyan
                        }
                        else -> if (MaterialTheme.colorScheme.background == BrutalColors.DarkPaper) {
                            BrutalColors.DarkTile
                        } else {
                            BrutalColors.White
                        }
                    },
                    borderWidth = if (index == currentPage) 4.dp else 2.dp,
                    shadowX = 3.dp,
                    shadowY = 3.dp
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (index == currentPage) BrutalColors.Ink else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
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
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    tilePositions: Map<String, NeoTilePosition>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    tileSizes: Map<String, NeoTileSize>,
    onTileSizeChange: (String, NeoTileSize) -> Unit,
    tileContentModes: Map<String, TileContentMode>,
    onTileContentModeChange: (String, TileContentMode) -> Unit
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
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
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
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = time.format(now),
                                        fontSize = 58.sp,
                                        lineHeight = 58.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BrutalColors.Ink
                                    )

                                    if (showDate) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = date.format(now).uppercase(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.5.sp,
                                            color = BrutalColors.Ink
                                        )
                                    }

                                }
                            }
                        }
                    )

                    if (showWeather) {
                        add(
                            NeoTileSpec(
                                id = "weather",
                                size = tileSizes["weather"] ?: NeoTileSize.WIDE,
                                label = "WEATHER"
                            ) {
                                NeoWeatherTile(
                                    context = context,
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
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "YOUR PHONE DOESN'T NEED TO LOOK CALM.",
                                            fontSize = 21.sp,
                                            lineHeight = 23.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.White
                                        )
                                    }
                                }
                            }
                        )
                    }

                    topApps.forEachIndexed { index, app ->
                        val tileColor = when (index % 8) {
                            0 -> BrutalColors.Pink
                            1 -> BrutalColors.Cyan
                            2 -> BrutalColors.Lime
                            3 -> BrutalColors.Orange
                            4 -> BrutalColors.Purple
                            5 -> BrutalColors.White
                            6 -> BrutalColors.Yellow
                            else -> BrutalColors.Pink
                        }

                        add(
                            NeoTileSpec(
                                id = "app_" + app.packageName + "_" + app.activityName,
                                size = tileSizes["app_" + app.packageName + "_" + app.activityName]
                                    ?: NeoTileSize.MEDIUM,
                                label = app.label.uppercase()
                            ) {
                                val appTileId = "app_" + app.packageName + "_" + app.activityName
                                AppTile(
                                    app = app,
                                    background = tileColor,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = { onLaunch(app) },
                                    contentMode = tileContentModes[appTileId] ?: TileContentMode.ICON_TEXT,
                                    randomSeed = appTileId.hashCode()
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
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "SYSTEM",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = BrutalColors.Ink
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = apps.size.toString() + " APPS DETECTED",
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.Ink
                                        )
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

                    if (tile.id.startsWith("app_")) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "CONTENT",
                            fontWeight = FontWeight.Black
                        )
                        listOf(
                            TileContentMode.ICON,
                            TileContentMode.ICON_TEXT,
                            TileContentMode.TEXT
                        ).forEach { mode ->
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    onTileContentModeChange(tile.id, mode)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = mode.label,
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
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "NEO QUOTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = BrutalColors.White
            )

            Text(
                text = "“$quote”",
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Black,
                color = BrutalColors.White
            )
        }
    }
}

@Composable
private fun AppTile(
    app: AppInfo,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit,
    contentMode: TileContentMode,
    randomSeed: Int
) {
    val random = remember(app.packageName, contentMode, randomSeed) {
        kotlin.random.Random(randomSeed + contentMode.ordinal * 997)
    }

    val alignments = listOf(
        Alignment.Center,
        Alignment.TopStart,
        Alignment.TopEnd,
        Alignment.BottomStart,
        Alignment.BottomEnd,
        Alignment.CenterStart,
        Alignment.CenterEnd
    )
    val contentAlignment = alignments[random.nextInt(alignments.size)]
    val padding = (8 + random.nextInt(17)).dp
    val iconDp = (48 + random.nextInt(49)).dp
    val baseTextSize = when (contentMode) {
        TileContentMode.ICON -> 0
        TileContentMode.ICON_TEXT -> 24
        TileContentMode.TEXT -> 34
    }
    val textSize = (baseTextSize + random.nextInt(9)).sp
    val textWeight = if (random.nextBoolean()) FontWeight.Black else FontWeight.ExtraBold
    val tilt = if (random.nextBoolean()) -1.5f else 1.5f

    BrutalBlock(
        modifier = modifier.clickable(onClick = onClick),
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = contentAlignment
        ) {
            when (contentMode) {
                TileContentMode.ICON -> {
                    Image(
                        bitmap = app.icon.toBitmap(128, 128).asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier
                            .width(iconDp)
                            .height(iconDp)
                            .graphicsLayer {
                                rotationZ = tilt
                            }
                    )
                }

                TileContentMode.ICON_TEXT -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap(128, 128).asImageBitmap(),
                            contentDescription = app.label,
                            modifier = Modifier
                                .width(iconDp)
                                .height(iconDp)
                                .graphicsLayer {
                                    rotationZ = tilt
                                }
                        )
                        Text(
                            text = app.label.uppercase(),
                            fontSize = textSize,
                            lineHeight = textSize * 0.9f,
                            fontWeight = textWeight,
                            color = BrutalColors.Ink,
                            maxLines = 2
                        )
                    }
                }

                TileContentMode.TEXT -> {
                    Text(
                        text = app.label.uppercase(),
                        fontSize = textSize,
                        lineHeight = textSize * 0.88f,
                        fontWeight = textWeight,
                        color = BrutalColors.Ink,
                        maxLines = 3
                    )
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

            SettingsSectionTitle("CLOCK")

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
