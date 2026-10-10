package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

private val Alphabet = ('A'..'Z').toList()

@Composable
fun AppsPage(
    apps: List<AppInfo>,
    favorites: Set<String>,
    onLaunch: (AppInfo) -> Unit,
    onOpenHome: () -> Unit
) {
    BackHandler(onBack = onOpenHome)

    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    // AppRepository already returns apps sorted by case-insensitive label.
    // Filtering preserves that order, so avoid sorting the entire result again
    // on every search-query change.
    val sortedApps = remember(apps, searchQuery) {
        apps.filter { searchQuery.isBlank() || it.label.contains(searchQuery, ignoreCase = true) }
    }
    val appPalette = remember(BrutalColors.activePreset) { BrutalColors.appPalette(0) }
    val listState = rememberLazyListState()
    var scrubLetter by remember { mutableStateOf<Char?>(null) }
    var contextApp by remember { mutableStateOf<AppInfo?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun firstIndexForLetter(letter: Char): Int {
        if (sortedApps.isEmpty()) return 0

        val index = sortedApps.indexOfFirst {
            (it.label.firstOrNull()?.uppercaseChar() ?: '#') >= letter
        }

        return if (index >= 0) index else sortedApps.lastIndex
    }

    LaunchedEffect(scrubLetter, sortedApps) {
        val letter = scrubLetter ?: return@LaunchedEffect
        if (sortedApps.isNotEmpty()) {
            listState.scrollToItem(firstIndexForLetter(letter))
        }
    }

    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val uiBackground = if (isDark) BrutalColors.Ink else BrutalColors.Paper
    val uiSurface = if (isDark) BrutalColors.DarkTile else BrutalColors.White
    val uiOnSurface = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink

    Box(
        modifier = Modifier
            .fillMaxSize()
             .background(Color.Transparent)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Cyan,
                borderWidth = 4.dp,
                shadowX = 7.dp,
                shadowY = 7.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ICON",
                            fontFamily = BrutalTypography.Display,
                            fontSize = NeoBrutalTokens.Type.Title,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 1.2.sp,
                            color = BrutalColors.Ink
                        )
                        Text(
                            text = "ALL YOUR APPS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.9.sp,
                            color = BrutalColors.Ink
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrutalLabel(
                            text = "%02d ITEMS".format(sortedApps.size),
                            background = BrutalColors.Yellow
                        )
                        BrutalBlock(
                            modifier = Modifier
                                .width(34.dp)
                                .height(34.dp)
                                .clickable {
                                    searchOpen = !searchOpen
                                    if (!searchOpen) searchQuery = ""
                                },
                            background = BrutalColors.Pink,
                            borderWidth = 3.dp,
                            shadowX = 3.dp,
                            shadowY = 3.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (searchOpen) "Close app search" else "Search apps",
                                    tint = BrutalColors.Ink,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (searchOpen) {
                Spacer(Modifier.height(8.dp))
                BrutalBlock(
                    modifier = Modifier.fillMaxWidth(),
                    background = uiSurface,
                    borderWidth = 3.dp,
                    borderColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
                    shadowX = 4.dp,
                    shadowY = 4.dp
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = uiOnSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        ),
                        decorationBox = { inner ->
                            if (searchQuery.isBlank()) {
                                Text(
                                    text = "SEARCH APPS…",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = uiOnSurface.copy(alpha = 0.45f)
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            if (sortedApps.isEmpty()) {
                BrutalBlock(
                    modifier = Modifier.fillMaxWidth(),
                    background = BrutalColors.Pink,
                    borderWidth = 3.dp,
                    shadowX = 5.dp,
                    shadowY = 5.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "NO LAUNCHABLE APPS",
                            fontFamily = BrutalTypography.Display,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.8.sp,
                            color = BrutalColors.Ink
                        )
                        Text(
                            text = "Install a launchable app and return to this page.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrutalColors.Ink
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 34.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(
                            items = sortedApps,
                            key = { _, app -> app.packageName + "/" + app.activityName }
                        ) { index, app ->
                            val iconBitmap = remember(app.packageName, app.icon) {
                                app.icon.toBitmap(64, 64).asImageBitmap()
                            }

                            val cardBackground = appPalette[index % appPalette.size]
                            val currentLetter = app.label.firstOrNull()?.uppercaseChar() ?: '#'
                            val previousLetter = sortedApps
                                .getOrNull(index - 1)
                                ?.label
                                ?.firstOrNull()
                                ?.uppercaseChar()
                                ?: '#'
                            val isFirstInLetterGroup =
                                index == 0 || currentLetter != previousLetter

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (isFirstInLetterGroup) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentLetter.toString(),
                                            fontFamily = BrutalTypography.Display,
                                            fontSize = 20.sp,
                                            lineHeight = 21.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = uiOnSurface
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(2.dp)
                                                .background(uiOnSurface)
                                        )
                                    }
                                }

                                BrutalPressableBlock(
                                    modifier = Modifier.fillMaxWidth(),
                                    background = cardBackground,
                                    borderWidth = 3.dp,
                                    borderColor = BrutalColors.Ink,
                                    shadowX = 5.dp,
                                    shadowY = 5.dp,
                                    onClick = { onLaunch(app) },
                                    onLongClick = { contextApp = app }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(NeoBrutalTokens.Spacing.Small),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            bitmap = iconBitmap,
                                            contentDescription = app.label,
                                            modifier = Modifier
                                                .width(50.dp)
                                                .height(50.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))

                                        Text(
                                            text = app.label.uppercase(),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 16.sp,
                                            lineHeight = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.Ink,
                                            maxLines = 2
                                        )

                                        if (favorites.contains(
                                                app.packageName + "/" + app.activityName
                                            )
                                        ) {
                                            BrutalBlock(
                                                modifier = Modifier
                                                    .width(12.dp)
                                                    .height(12.dp),
                                                background = BrutalColors.Yellow,
                                                borderWidth = 2.dp,
                                                shadowX = 0.dp,
                                                shadowY = 0.dp
                                            ) {}
                                        }
                                    }
                                }
                            }
                        }
                        }
                    BrutalBlock(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .pointerInput(sortedApps) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val index = (
                                            offset.y / size.height.coerceAtLeast(1)
                                        ).coerceIn(0f, 0.999999f) * Alphabet.size
                                        scrubLetter = Alphabet[index.toInt().coerceIn(0, 25)]
                                    },
                                    onDragEnd = {
                                        scrubLetter = null
                                    },
                                    onDragCancel = {
                                        scrubLetter = null
                                    }
                                ) { change, _ ->
                                    change.consume()
                                    val fraction = (
                                        change.position.y / size.height.coerceAtLeast(1)
                                    ).coerceIn(0f, 0.999999f)
                                    scrubLetter = Alphabet[
                                        (fraction * Alphabet.size)
                                            .toInt()
                                            .coerceIn(0, 25)
                                    ]
                                }
                            },
                        background = BrutalColors.Yellow,
                        borderWidth = 3.dp,
                        shadowX = 4.dp,
                        shadowY = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 7.dp, horizontal = 2.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Alphabet.forEach { letter ->
                                Text(
                                    text = letter.toString(),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink
                                )
                            }
                        }
                    }

                    scrubLetter?.let { letter ->
                        BrutalBlock(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 32.dp)
                                .width(64.dp)
                                .height(64.dp),
                            background = BrutalColors.Pink,
                            borderWidth = 4.dp,
                            shadowX = 5.dp,
                            shadowY = 5.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    contextApp?.let { app ->
        AlertDialog(
            onDismissRequest = { contextApp = null },
            containerColor = uiSurface,
            titleContentColor = uiOnSurface,
            textContentColor = uiOnSurface,
            title = {
                Text(text = app.label.uppercase(), fontFamily = BrutalTypography.Display, fontWeight = FontWeight.Normal)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BrutalActionButton(
                        title = "OPEN APP", background = BrutalColors.Cyan,
                        onClick = { onLaunch(app); contextApp = null }
                    )
                    BrutalActionButton(
                        title = "APP INFO", background = BrutalColors.Cyan,
                        onClick = {
                            runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.parse("package:" + app.packageName) }) }
                            contextApp = null
                        }
                    )
                    if (app.packageName != context.packageName) {
                        BrutalActionButton(
                            title = "UNINSTALL", background = BrutalColors.Pink,
                            onClick = {
                                val uninstallIntent = Intent(
                                    Intent.ACTION_UNINSTALL_PACKAGE,
                                    Uri.parse("package:" + app.packageName)
                                )
                                runCatching {
                                    context.startActivity(uninstallIntent)
                                }.onFailure {
                                    // Some Android builds expose uninstall only from App info.
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.parse("package:" + app.packageName)
                                        }
                                    )
                                }
                                contextApp = null
                            }
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}
