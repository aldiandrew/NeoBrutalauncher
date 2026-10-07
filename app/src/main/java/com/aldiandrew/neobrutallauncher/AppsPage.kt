package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AppsPage(
    apps: List<AppInfo>,
    favorites: Set<String>,
    onToggleFavorite: (AppInfo) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    BackHandler(onBack = onOpenHome)

    val filtered = remember(apps, query) {
        if (query.isBlank()) {
            apps.sortedBy { it.label.lowercase() }
        } else {
            apps
                .filter {
                    it.label.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
                }
                .sortedBy { it.label.lowercase() }
        }
    }

    val uiBackground = androidx.compose.material3.MaterialTheme.colorScheme.background
    val uiSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val uiOnSurface = androidx.compose.material3.MaterialTheme.colorScheme.onSurface

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
                background = BrutalColors.Cyan,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column {
                    Text(
                        text = "APPS PAGE",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = filtered.size.toString() + " APPS / A-Z",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = BrutalColors.Ink
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            BrutalBlock(
                modifier = Modifier.height(48.dp).width(48.dp).clickable(onClick = onRefresh),
                background = BrutalColors.Yellow,
                borderWidth = 3.dp,
                shadowX = 3.dp,
                shadowY = 3.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Refresh apps",
                    tint = BrutalColors.Ink,
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                )
            }

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = uiOnSurface
                )
            }
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
                    contentDescription = "Search apps",
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
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = uiOnSurface
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = uiOnSurface,
                unfocusedTextColor = uiOnSurface,
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
                        text = if (query.isBlank()) "NO LAUNCHABLE APPS" else "NO MATCHES",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                    Text(
                        text = "Install or refresh apps, then return here.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalColors.Ink
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier.width(50.dp).height(50.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val firstLetter = app.label.firstOrNull()?.uppercase() ?: "#"
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
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Pinned",
                                    tint = BrutalColors.Orange,
                                    modifier = Modifier.width(24.dp).height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
