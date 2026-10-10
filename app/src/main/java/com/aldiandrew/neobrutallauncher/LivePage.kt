package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
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
    val liveClockTileHeight = 112.dp
    var weatherRefreshToken by remember { mutableIntStateOf(0) }

    val timePattern = when {
        use24Hour -> "HH:mm"
        showAmPm -> "hh:mm a"
        else -> "hh:mm"
    }
    val timeText = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
    val dateText = SimpleDateFormat(
        "EEE, d MMM yyyy",
        Locale.getDefault()
    ).format(now).uppercase(Locale.getDefault())

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
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
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
                            color = headerText,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = dateText,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = headerText,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(84.dp)
                            .padding(vertical = 3.dp)
                            .border(3.dp, BrutalColors.Ink)
                            .background(BrutalColors.Cyan)
                            .padding(5.dp)
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp)
                                    .background(BrutalColors.Red)
                                    .border(3.dp, BrutalColors.Ink),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(27.dp)
                                        .background(BrutalColors.Yellow)
                                        .border(3.dp, BrutalColors.Ink)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(13.dp)
                                    .background(BrutalColors.Pink)
                                    .border(2.dp, BrutalColors.Ink)
                            )
                            Text(
                                text = "NB",
                                modifier = Modifier.align(Alignment.BottomEnd),
                                fontFamily = BrutalTypography.Display,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink
                            )
                        }
                    }
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
                        modifier = Modifier.weight(1f).height(148.dp),
                        emphasized = false,
                        paletteIndex = quoteRotation + 1,
                        showLabel = true
                    )
                    NeoWeatherTile(
                        context = context,
                        refreshToken = weatherRefreshToken,
                        modifier = Modifier
                            .weight(1f)
                            .height(148.dp)
                            .clickable { weatherRefreshToken++ }
                    )
                }
            } else {
                NeoQuoteTile(
                    quote = NeoQuotes.pairForRotation(quoteRotation, customQuotes).second,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
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
                modifier = Modifier.fillMaxWidth().height(116.dp),
                background = if (isDark) BrutalColors.DarkTile else BrutalColors.Yellow,
                textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
                onChooseApp = { showChatAppPicker = true }
            )
        }

        item(key = "live-music") {
            NeoMusicTile(
                context = context,
                modifier = Modifier.fillMaxWidth().height(124.dp),
                background = if (isDark) BrutalColors.DarkTile else BrutalColors.Cyan,
                textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink,
            )
        }

        item(key = "live-calendar") {
            NeoCalendarTile(
                context = context,
                modifier = Modifier.fillMaxWidth().height(194.dp),
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
