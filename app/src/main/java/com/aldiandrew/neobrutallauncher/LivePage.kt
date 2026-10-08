package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LivePage(
    apps: List<AppInfo>,
    customQuotes: List<String>,
    selectedChatPackages: List<String>,
    onSelectChatPackage: (String) -> Unit,
    onOpenHome: () -> Unit
) {
    BackHandler(onBack = onOpenHome)

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { LauncherPreferences(context) }
    var showChatAppPicker by remember { mutableStateOf(false) }
    val chatCandidates = remember(apps) {
        apps.groupBy { it.packageName }
            .values
            .mapNotNull { it.firstOrNull() }
            .filter { it.packageName != context.packageName }
            .sortedBy { it.label.lowercase() }
    }
    val now = rememberMinuteClock()
    val quoteRotation = rememberLiveTileData(
        tileId = "live-quotes",
        refreshIntervalMillis = 30L * 60L * 1000L,
        initialValue = (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    ) {
        (System.currentTimeMillis() / (30L * 60L * 1000L)).toInt()
    }.value ?: 0
    val isDark = MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val accentSurface = if (isDark) BrutalColors.DarkTile else BrutalColors.Yellow
    val pageText = MaterialTheme.colorScheme.onBackground

    val dateText = SimpleDateFormat(
        "EEEE / d MMMM yyyy",
        Locale.ENGLISH
    ).format(now)

    val notes = remember { preferences.noteItems().takeLast(3).reversed() }
    val tasks = remember { preferences.taskItems().takeLast(3).reversed() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
             .background(Color.Transparent)
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
                borderColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
                shadowX = 7.dp,
                shadowY = 7.dp,
                shadowColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "LIVE",
                        fontFamily = BrutalTypography.Display,
                        fontSize = NeoBrutalTokens.Type.Title,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) BrutalColors.Red else pageText
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                        fontSize = NeoBrutalTokens.Type.Hero,
                        lineHeight = NeoBrutalTokens.Type.Hero,
                        fontWeight = FontWeight.Black,
                        color = pageText
                    )
                    Text(text = dateText, fontSize = NeoBrutalTokens.Type.Label, fontWeight = FontWeight.Black, color = pageText)
                }
            }
        }

        item {
            NeoCalendarTile(
                context = context,
                modifier = Modifier.fillMaxWidth().height(224.dp),
                background = if (isDark) BrutalColors.Pink else BrutalColors.Cyan,
                textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink
            )
        }

        item {
            NeoChatNotificationTile(
                context = context,
                packageName = selectedChatPackages.firstOrNull(),
                modifier = Modifier.fillMaxWidth().height(126.dp),
                background = if (isDark) BrutalColors.Pink else BrutalColors.Yellow,
                textColor = BrutalColors.Ink,
                onChooseApp = { showChatAppPicker = true }
            )
        }

        item {
            NeoMusicTile(
                context = context,
                modifier = Modifier.fillMaxWidth().height(126.dp),
                background = if (isDark) BrutalColors.DarkTile else BrutalColors.Cyan,
                textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
            )
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
                quote = NeoQuotes.pairForRotation(quoteRotation, customQuotes).second,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                emphasized = true,
                paletteIndex = quoteRotation + 1
            )
        }
    }

    if (showChatAppPicker) {
        AlertDialog(
            onDismissRequest = { showChatAppPicker = false },
            title = {
                Text(
                    text = "CHOOSE CHAT APP",
                    fontFamily = BrutalTypography.Display,
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SELECT THE APP USED BY THE LIVE CHAT TILE.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (chatCandidates.isEmpty()) {
                        Text(
                            text = "NO LAUNCHABLE APPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(360.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(
                                items = chatCandidates,
                                key = { it.packageName + "/" + it.activityName }
                            ) { app ->
                                BrutalBlock(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectChatPackage(app.packageName)
                                            showChatAppPicker = false
                                        },
                                    background = BrutalColors.Yellow,
                                    borderWidth = 3.dp,
                                    shadowX = 3.dp,
                                    shadowY = 3.dp
                                ) {
                                    Text(
                                        text = app.label.uppercase(Locale.ENGLISH),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BrutalColors.Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
