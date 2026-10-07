package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch

private val Alphabet = ('A'..'Z').toList()

@Composable
fun AppsPage(
    apps: List<AppInfo>,
    favorites: Set<String>,
    onToggleFavorite: (AppInfo) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onOpenHome: () -> Unit
) {
    BackHandler(onBack = onOpenHome)

    val sortedApps = remember(apps) {
        apps.sortedBy { it.label.lowercase() }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var scrubLetter by remember { mutableStateOf<Char?>(null) }

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

    val uiBackground = androidx.compose.material3.MaterialTheme.colorScheme.background
    val uiSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val uiOnSurface = androidx.compose.material3.MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(uiBackground)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Cyan,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column {
                    Text(
                        text = "APPS",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = sortedApps.size.toString() + " APPS / LONG-PRESS TO PIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = BrutalColors.Ink
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
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
                        items(
                            items = sortedApps,
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                                        contentDescription = app.label,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        val firstLetter =
                                            app.label.firstOrNull()?.uppercase() ?: "#"

                                        Text(
                                            text = firstLetter,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BrutalColors.Orange
                                        )
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
                                            fontSize = 9.sp,
                                            lineHeight = 11.sp,
                                            color = uiOnSurface.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }

                                    if (favorites.contains(app.packageName + "/" + app.activityName)) {
                                        BrutalBlock(
                                            modifier = Modifier
                                                .width(12.dp)
                                                .height(12.dp),
                                            background = BrutalColors.Orange,
                                            borderWidth = 2.dp,
                                            shadowX = 0.dp,
                                            shadowY = 0.dp
                                        ) {}
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
                                .align(Alignment.Center)
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
}
