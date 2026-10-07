package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NeoBrutalLauncherApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AppRepository(context) }
    var apps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var drawerOpen by remember { mutableStateOf(false) }

    fun refreshApps() {
        apps = repository.loadApps()
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    LaunchedEffect(drawerOpen) {
        if (drawerOpen) {
            refreshApps()
        }
    }

    NeoBrutalTheme {
        if (drawerOpen) {
            AppDrawer(
                apps = apps,
                onClose = { drawerOpen = false },
                onRefresh = { refreshApps() },
                onLaunch = repository::launch
            )
        } else {
            HomeScreen(
                apps = apps,
                onOpenDrawer = { drawerOpen = true },
                onLaunch = repository::launch
            )
        }
    }
}

@Composable
private fun HomeScreen(
    apps: List<AppInfo>,
    onOpenDrawer: () -> Unit,
    onLaunch: (AppInfo) -> Unit
) {
    var showAbout by remember { mutableStateOf(false) }
    val topApps = apps.take(6)
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val date = remember { SimpleDateFormat("EEE / dd MMM", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalColors.Paper)
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
                        .width(70.dp)
                        .clickable { showAbout = true },
                    background = BrutalColors.Pink,
                    borderWidth = 3.dp,
                    shadowX = 4.dp,
                    shadowY = 4.dp
                ) {
                    Text(
                        text = "V0.1",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                }
            }

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Yellow,
                borderWidth = 4.dp,
                shadowX = 8.dp,
                shadowY = 8.dp
            ) {
                Column {
                    Text(
                        text = time.format(Date()),
                        fontSize = 58.sp,
                        lineHeight = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = date.format(Date()).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = BrutalColors.Ink
                    )
                }
            }

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Ink,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Text(
                    text = "YOUR PHONE DOESN'T NEED TO LOOK CALM.",
                    fontSize = 21.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.White
                )
            }

            if (topApps.isNotEmpty()) {
                AppRow(topApps.take(2), BrutalColors.Pink, BrutalColors.Cyan, onLaunch)
                AppRow(topApps.drop(2).take(2), BrutalColors.Lime, BrutalColors.Orange, onLaunch)
                AppRow(topApps.drop(4).take(2), BrutalColors.Purple, BrutalColors.White, onLaunch)
            } else {
                BrutalBlock(
                    modifier = Modifier.fillMaxWidth(),
                    background = BrutalColors.White,
                    borderWidth = 3.dp,
                    shadowX = 6.dp,
                    shadowY = 6.dp
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

            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.White,
                borderWidth = 3.dp,
                shadowX = 6.dp,
                shadowY = 6.dp
            ) {
                Column {
                    Text(
                        text = "SYSTEM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = apps.size.toString() + " APPS DETECTED",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            BrutalBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDrawer),
                background = BrutalColors.Ink,
                borderWidth = 4.dp,
                shadowX = 8.dp,
                shadowY = 8.dp
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

            Spacer(Modifier.height(12.dp))
        }
    }

    if (showAbout) {
        Dialog(onDismissRequest = { showAbout = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 8.dp, y = 8.dp)
                        .background(BrutalColors.Ink)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(4.dp, BrutalColors.Ink)
                        .background(BrutalColors.Yellow)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ABOUT / NEO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = "NEO BRUTAL LAUNCHER",
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = "A launcher that rejects the idea that your home screen has to look calm.",
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = "CORE BUILD 0.1.0",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = BrutalColors.Ink
                    )
                    BrutalBlock(
                        modifier = Modifier.fillMaxWidth(),
                        background = BrutalColors.Cyan,
                        borderWidth = 3.dp,
                        shadowX = 4.dp,
                        shadowY = 4.dp
                    ) {
                        Text(
                            text = "NEO-BRUTALISM / METRO DIRECTION",
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BrutalColors.Ink
                        )
                    }
                }
            }
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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
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
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onLaunch: (AppInfo) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalColors.Paper)
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

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = BrutalColors.Ink
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = BrutalColors.Ink
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        BrutalBlock(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { focusRequester.requestFocus() },
            background = BrutalColors.Ink,
            borderWidth = 3.dp,
            shadowX = 4.dp,
            shadowY = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search apps",
                    tint = BrutalColors.White,
                    modifier = Modifier
                        .width(28.dp)
                        .height(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "SEARCH APPS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = BrutalColors.White
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = BrutalColors.Ink
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = BrutalColors.Ink
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = "TYPE APP NAME...",
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink.copy(alpha = 0.65f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = BrutalColors.Ink,
                unfocusedTextColor = BrutalColors.Ink,
                focusedContainerColor = BrutalColors.White,
                unfocusedContainerColor = BrutalColors.White,
                focusedIndicatorColor = BrutalColors.Ink,
                unfocusedIndicatorColor = BrutalColors.Ink,
                cursorColor = BrutalColors.Ink
            )
        )

        Spacer(Modifier.height(14.dp))

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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filtered,
                    key = { it.packageName + "/" + it.activityName }
                ) { app ->
                    BrutalBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLaunch(app) },
                        background = BrutalColors.White,
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
                                    color = BrutalColors.Ink
                                )
                                Text(
                                    text = app.packageName,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    color = BrutalColors.Ink
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
