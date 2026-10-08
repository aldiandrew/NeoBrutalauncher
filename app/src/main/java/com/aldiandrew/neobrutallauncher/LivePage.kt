package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LivePage(
    apps: List<AppInfo>,
    onLaunch: (AppInfo) -> Unit,
    onOpenHome: () -> Unit
) {
    BackHandler(onBack = onOpenHome)

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { LauncherPreferences(context) }
    val now = rememberMinuteClock()
    val quoteRotation = rememberLiveTileData(
        tileId = "live-quotes",
        refreshIntervalMillis = 30L * 60L * 1000L,
        initialValue = (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    ) {
        (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    }.value ?: 0
    val isDark = MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val neutralSurface = if (isDark) BrutalColors.DarkTile else MaterialTheme.colorScheme.background
    val accentSurface = if (isDark) BrutalColors.DarkTile else BrutalColors.Orange
    val pageText = MaterialTheme.colorScheme.onBackground
    val secondarySurface = if (isDark) BrutalColors.DarkTile else BrutalColors.Yellow

    val launchCounts = remember(apps) {
        preferences.appLaunchCounts()
    }
    val liveApps = remember(apps, launchCounts) {
        apps.sortedWith(
            compareByDescending<AppInfo> {
                launchCounts[it.packageName + "/" + it.activityName] ?: 0
            }.thenBy { it.label.lowercase() }
        ).take(6)
    }

    val notes = remember { preferences.noteItems().takeLast(3).reversed() }
    val tasks = remember { preferences.taskItems().takeLast(3).reversed() }

    val dateText = SimpleDateFormat(
        "EEEE / d MMMM yyyy",
        Locale.ENGLISH
    ).format(now)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 72.dp,
                start = 18.dp,
                end = 18.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = accentSurface,
                borderWidth = 4.dp,
                shadowX = 7.dp,
                shadowY = 7.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "LIVE",
                        fontFamily = BrutalTypography.Display,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = pageText
                    )
                    Text(text = dateText, fontSize = 11.sp, fontWeight = FontWeight.Black, color = pageText)
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                        fontSize = 42.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = pageText
                    )
                }
            }
        }

        item {
            NeoMusicTile(
                context = context,
                modifier = Modifier.fillMaxWidth().height(126.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BatteryTile(
                    context = context,
                    modifier = Modifier.weight(1f).height(150.dp),
                    background = secondarySurface
                )
                NeoNetworkTile(
                    context = context,
                    modifier = Modifier.weight(1f).height(150.dp)
                )
            }
        }

        item {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = neutralSurface,
                borderWidth = 3.dp,
                borderColor = pageText,
                shadowX = 0.dp,
                shadowY = 0.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LIVE ACTIVITY",
                        fontFamily = BrutalTypography.Display,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = pageText
                    )
                    liveApps.forEachIndexed { index, app ->
                        val count = launchCounts[app.packageName + "/" + app.activityName] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaunch(app) }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%02d".format(Locale.ENGLISH, index + 1),
                                modifier = Modifier.width(28.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) BrutalColors.Cyan else BrutalColors.Orange
                            )
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = pageText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$count LAUNCH",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = pageText
                            )
                        }
                    }
                }
            }
        }

        item {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = MaterialTheme.colorScheme.background,
                borderWidth = 3.dp,
                borderColor = MaterialTheme.colorScheme.onBackground,
                shadowX = 0.dp,
                shadowY = 0.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "LATEST NOTES / TASKS",
                        fontFamily = BrutalTypography.Display,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = pageText
                    )
                    if (notes.isEmpty() && tasks.isEmpty()) {
                        Text(
                            text = "NO LOCAL ACTIVITY YET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = pageText
                        )
                    } else {
                        notes.forEach {
                            Text(
                                text = "NOTE  /  " + it.text,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = pageText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        tasks.forEach {
                            Text(
                                text = "TASK  /  " +
                                    if (it.checked) "✓ " + it.text else "□ " + it.text,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = pageText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        item {
            NeoQuoteTile(
                quote = NeoQuotes.pairForRotation(quoteRotation).second,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                emphasized = true
            )
        }
    }
}
