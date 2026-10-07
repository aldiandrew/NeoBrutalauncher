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
    var drawerOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }

    var themePreference by remember { mutableStateOf(preferences.theme()) }
    var use24Hour by remember { mutableStateOf(preferences.use24Hour()) }
    var showDate by remember { mutableStateOf(preferences.showDate()) }
    var homeAppCount by remember { mutableStateOf(preferences.homeAppCount()) }
    var showTagline by remember { mutableStateOf(preferences.showTagline()) }
    var showAppCount by remember { mutableStateOf(preferences.showAppCount()) }
    var showWeather by remember { mutableStateOf(preferences.showWeather()) }
    var showQuote by remember { mutableStateOf(preferences.showQuote()) }
    var favorites by remember { mutableStateOf(preferences.favorites()) }
    var tilePositions by remember { mutableStateOf(preferences.tilePositions()) }
    var tileSizes by remember { mutableStateOf(preferences.tileSizes()) }

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

    LaunchedEffect(drawerOpen) {
        if (drawerOpen) {
            refreshApps()
        }
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
                    onRefreshApps = { refreshApps() },
                    onClearFavorites = {
                        favorites = emptySet()
                        preferences.clearFavorites()
                    }
                )
            }
        }

        drawerOpen -> {
            NeoBrutalTheme(themePreference = themePreference) {
                AppDrawer(
                    apps = apps,
                    favorites = favorites,
                    onClose = { drawerOpen = false },
                    onToggleFavorite = { app ->
                        val key = app.packageName + "/" + app.activityName
                        val updated = favorites.toMutableSet()
                        if (!updated.add(key)) {
                            updated.remove(key)
                        }
                        favorites = updated
                        preferences.setFavorites(updated)
                    },
                    onLaunch = repository::launch
                )
            }
        }

        else -> {
            NeoBrutalTheme(themePreference = themePreference) {
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
                    onOpenDrawer = { drawerOpen = true },
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
                    onTileLongPress = { selectedTile = it }
                )
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
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    tilePositions: Map<String, NeoTilePosition>,
    onTilePositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    tileSizes: Map<String, NeoTileSize>,
    onTileSizeChange: (String, NeoTileSize) -> Unit,
    onTileLongPress: (NeoTileSpec) -> Unit
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
                            size = tileSizes["clock"] ?: NeoTileSize.FOUR_BY_TWO,
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

                                    if (showWeather) {
                                        WeatherContent(context = context)
                                    }
                                }
                            }
                        }
                    )

                    if (showQuote) {
                        add(
                            NeoTileSpec(
                                id = "quote",
                                size = tileSizes["quote"] ?: NeoTileSize.FOUR_BY_TWO,
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
                                size = tileSizes["tagline"] ?: NeoTileSize.FOUR_BY_TWO,
                                label = "TAGLINE"
                            ) {
                                BrutalBlock(
                                    modifier = Modifier.fillMaxSize(),
                                    background = darkTileBackground,
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
                                    ?: NeoTileSize.TWO_BY_TWO,
                                label = app.label.uppercase()
                            ) {
                                AppTile(
                                    app = app,
                                    background = tileColor,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = { onLaunch(app) }
                                )
                            }
                        )
                    }

                    if (topApps.isEmpty()) {
                        add(
                            NeoTileSpec(
                                id = "empty-apps",
                                size = tileSizes["empty-apps"] ?: NeoTileSize.FOUR_BY_TWO,
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

                    if (showAppCount) {
                        add(
                            NeoTileSpec(
                                id = "system",
                                size = tileSizes["system"] ?: NeoTileSize.FOUR_BY_TWO,
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

                    add(
                        NeoTileSpec(
                            id = "all-apps",
                            size = tileSizes["all-apps"] ?: NeoTileSize.FOUR_BY_TWO,
                            label = "ALL APPS"
                        ) {
                            BrutalBlock(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(onClick = onOpenDrawer),
                                background = darkTileBackground,
                                borderWidth = 4.dp,
                                shadowX = 8.dp,
                                shadowY = 8.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "ALL APPS  →",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BrutalColors.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    )
                },
                positions = tilePositions,
                onPositionsChange = onTilePositionsChange,
                onTileLongPress = { onTileLongPress(it) },
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
                        NeoTileSize.ONE_BY_ONE,
                        NeoTileSize.TWO_BY_ONE,
                        NeoTileSize.ONE_BY_TWO,
                        NeoTileSize.TWO_BY_TWO,
                        NeoTileSize.THREE_BY_TWO,
                        NeoTileSize.TWO_BY_THREE,
                        NeoTileSize.THREE_BY_THREE,
                        NeoTileSize.FOUR_BY_ONE,
                        NeoTileSize.FOUR_BY_TWO,
                        NeoTileSize.FOUR_BY_THREE,
                        NeoTileSize.FOUR_BY_FOUR
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
private fun WeatherContent(
    context: android.content.Context
) {

    val weatherLive = rememberLiveTileData(
        tileId = "weather",
        refreshIntervalMillis = 15 * 60 * 1000L,
        loader = {
            WeatherRepository.loadCurrentWeather(context)
        }
    )

    val weatherIcon = when (weatherLive.value?.weatherCode) {
        0 -> Icons.Default.WbSunny
        1, 2, 3, 45, 48 -> Icons.Default.Cloud
        51, 53, 55, 56, 57 -> Icons.Default.WaterDrop
        61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Default.Umbrella
        71, 73, 75, 77, 85, 86 -> Icons.Default.AcUnit
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "WEATHER / OPEN-METEO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = BrutalColors.Ink
        )

        when {
            weatherLive.value != null -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = weatherIcon,
                        contentDescription = weatherLive.value!!.description,
                        tint = BrutalColors.Ink,
                        modifier = Modifier
                            .width(52.dp)
                            .height(52.dp)
                    )
                    Column {
                        Text(
                            text = "${weatherLive.value!!.temperatureC.toInt()}°C",
                            fontSize = 22.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink
                        )
                        Text(
                            text = weatherLive.value!!.description.uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink
                        )
                    }
                }
            }

            weatherLive.error != null -> {
                Text(
                    text = "LOCATION / NETWORK NEEDED",
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
            }

            weatherLive.loading -> {
                Text(
                    text = "LOADING WEATHER...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
            }

            else -> {
                Text(
                    text = "WEATHER UNAVAILABLE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
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
private fun AppRow(
    apps: List<AppInfo>,
    firstColor: Color,
    secondColor: Color,
    onLaunch: (AppInfo) -> Unit
) {
    if (apps.size < 2) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppTile(
            app = apps[0],
            background = firstColor,
            modifier = Modifier.weight(1f),
            onClick = { onLaunch(apps[0]) }
        )
        AppTile(
            app = apps[1],
            background = secondColor,
            modifier = Modifier.weight(1f),
            onClick = { onLaunch(apps[1]) }
        )
    }
}

@Composable
private fun AppTile(
    app: AppInfo,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    BrutalBlock(
        modifier = modifier.clickable(onClick = onClick),
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                    .width(54.dp)
                    .height(54.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = app.label.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Black,
                color = BrutalColors.Ink,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AppDrawer(
    apps: List<AppInfo>,
    favorites: Set<String>,
    onClose: () -> Unit,
    onToggleFavorite: (AppInfo) -> Unit,
    onLaunch: (AppInfo) -> Unit
) {
    BackHandler(onBack = onClose)
    var query by remember { mutableStateOf("") }

    val filtered = remember(apps, query) {
        if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }
    }

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
            BrutalBlock(
                modifier = Modifier.weight(1f),
                background = BrutalColors.Yellow,
                borderWidth = 3.dp,
                shadowX = 4.dp,
                shadowY = 4.dp
            ) {
                Column {
                    Text(
                        text = "ALL APPS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = filtered.size.toString() + " RESULTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "LONG-PRESS TO PIN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = uiOnSurface
            )
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = uiOnSurface
                )
            },
            placeholder = {
                Text(
                    text = "SEARCH APPS...",
                    fontWeight = FontWeight.Black,
                    color = uiOnSurface.copy(alpha = 0.65f)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { query = "" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = uiOnSurface
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = BrutalColors.Ink,
                unfocusedTextColor = BrutalColors.Ink,
                focusedContainerColor = uiSurface,
                unfocusedContainerColor = uiSurface,
                focusedIndicatorColor = uiOnSurface,
                unfocusedIndicatorColor = uiOnSurface,
                cursorColor = uiOnSurface
            )
        )

        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Pink,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (query.isBlank()) {
                            "NO LAUNCHABLE APPS"
                        } else {
                            "NO MATCHES"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = if (query.isBlank()) {
                            "The launcher service returned an empty app list."
                        } else {
                            "Try another app name or package."
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalColors.Ink
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filtered,
                    key = { it.packageName + "/" + it.activityName }
                ) { app ->
                    BrutalBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onLaunch(app) },
                                onLongClick = { onToggleFavorite(app) }
                            ),
                        background = uiSurface,
                        borderWidth = 3.dp,
                        shadowX = 4.dp,
                        shadowY = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap(80, 80).asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier
                                    .width(46.dp)
                                    .height(46.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label.uppercase(),
                                    fontSize = 16.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = uiOnSurface,
                                    maxLines = 2
                                )
                                Text(
                                    text = app.packageName,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    color = uiOnSurface,
                                    maxLines = 1
                                )
                            }

                            if (favorites.contains(app.packageName + "/" + app.activityName)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    tint = BrutalColors.Orange,
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
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
    appsCount: Int,
    themePreference: ThemePreference,
    use24Hour: Boolean,
    showDate: Boolean,
    homeAppCount: Int,
    showTagline: Boolean,
    showAppCount: Boolean,
    showWeather: Boolean,
    showQuote: Boolean,
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
                description = "Show current temperature and conditions inside the clock tile. Location and Internet access are required.",
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
                        text = "Long-press any app in the drawer to pin or unpin it. Pinned apps appear first on the home screen.",
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
