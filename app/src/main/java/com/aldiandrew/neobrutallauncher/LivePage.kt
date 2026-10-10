package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableIntStateOf
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
    showWeather: Boolean,
    use24Hour: Boolean,
    showAmPm: Boolean,
    onSelectChatPackage: (String) -> Unit,
    onOpenHome: () -> Unit
) {
    BackHandler(onBack = onOpenHome)

    val context = androidx.compose.ui.platform.LocalContext.current
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
    val headerText = BrutalColors.Ink
    val liveClockTileHeight = 128.dp
    var weatherRefreshToken by remember { mutableIntStateOf(0) }

    val timePattern = when {
        use24Hour -> "HH:mm"
        showAmPm -> "hh:mm a"
        else -> "hh:mm"
    }
    val timeText = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
    val dateText = SimpleDateFormat(
        "EEEE, d MMMM yyyy",
        Locale.getDefault()
    ).format(now)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "live-clock") {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth().height(liveClockTileHeight),
                background = BrutalColors.Yellow,
                borderWidth = 3.dp,
                borderColor = BrutalColors.Ink,
                shadowX = 5.dp,
                shadowY = 5.dp,
                shadowColor = BrutalColors.Ink
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "LIVE",
                        fontFamily = BrutalTypography.Display,
                        fontSize = NeoBrutalTokens.Type.Title,
                        fontWeight = FontWeight.Normal,
                        color = BrutalColors.Red
                    )
                    Text(
                        text = timeText,
                        fontSize = 38.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = headerText
                    )
                    Text(
                        text = dateText,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = headerText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item(key = "live-quote-weather") {
            if (showWeather) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeoQuoteTile(
                        quote = NeoQuotes.pairForRotation(quoteRotation, customQuotes).second,
                        modifier = Modifier.weight(1f).height(168.dp),
                        emphasized = false,
                        paletteIndex = quoteRotation + 1,
                        showLabel = true
                    )
                    NeoWeatherTile(
                        context = context,
                        refreshToken = weatherRefreshToken,
                        modifier = Modifier
                            .weight(1f)
                            .height(168.dp)
                            .clickable { weatherRefreshToken++ }
                    )
                }
            } else {
                NeoQuoteTile(
                    quote = NeoQuotes.pairForRotation(quoteRotation, customQuotes).second,
                    modifier = Modifier.fillMaxWidth().height(112.dp),
                    emphasized = true,
                    paletteIndex = quoteRotation + 1,
                    showLabel = true
                )
            }
        }

        item(key = "live-chat") {
            NeoChatNotificationTile(
                context = context,
                packageName = selectedChatPackages.firstOrNull(),
                modifier = Modifier.fillMaxWidth().height(128.dp),
                background = if (isDark) BrutalColors.DarkTile else BrutalColors.Yellow,
                textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
                onChooseApp = { showChatAppPicker = true }
            )
        }

        item(key = "live-music") {
            NeoMusicTile(
                context = context,
                modifier = Modifier.fillMaxWidth().height(142.dp),
                background = if (isDark) BrutalColors.DarkTile else BrutalColors.Cyan,
                textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
                liveLayout = true
            )
        }

        item(key = "live-calendar") {
            NeoCalendarTile(
                context = context,
                modifier = Modifier.fillMaxWidth().height(208.dp),
                background = if (isDark) BrutalColors.DarkTile else BrutalColors.Cyan,
                textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
