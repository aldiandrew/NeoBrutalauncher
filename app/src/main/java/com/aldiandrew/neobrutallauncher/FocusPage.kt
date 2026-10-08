package com.aldiandrew.neobrutallauncher

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FocusPage(
    apps: List<AppInfo>,
    favorites: Set<String>,
    onLaunch: (AppInfo) -> Unit,
    onOpenHome: () -> Unit
) {
    BackHandler(onBack = onOpenHome)

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { LauncherPreferences(context) }
    val now = rememberMinuteClock()

    var selectedMinutes by remember { mutableIntStateOf(25) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    var noteDraft by remember { mutableStateOf("") }

    LaunchedEffect(running) {
        while (running && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        }
        if (remainingSeconds <= 0) running = false
    }

    val focusApps = remember(apps, favorites) {
        val favoriteApps = apps.filter {
            favorites.contains(it.packageName + "/" + it.activityName)
        }
        (favoriteApps + apps.filterNot { favoriteApps.contains(it) })
            .distinctBy { it.packageName + "/" + it.activityName }
            .take(4)
    }

    val dateText = remember(now) {
        SimpleDateFormat("EEE / d MMM yyyy", Locale.ENGLISH).format(now)
    }
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timerText = "%02d:%02d".format(Locale.ENGLISH, minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                background = BrutalColors.Cyan,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "FOCUS",
                        fontFamily = BrutalTypography.Display,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "DISCIPLINE CREATES FREEDOM.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    Text(text = dateText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Pink,
                borderWidth = 4.dp,
                shadowX = 7.dp,
                shadowY = 7.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (running) "FOCUS IN PROGRESS" else "READY TO FOCUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = timerText,
                        fontSize = 58.sp,
                        lineHeight = 58.sp,
                        fontWeight = FontWeight.Black
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(25, 50, 90).forEach { preset ->
                            BrutalBlock(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        running = false
                                        selectedMinutes = preset
                                        remainingSeconds = preset * 60
                                    },
                                background = if (selectedMinutes == preset) {
                                    BrutalColors.Yellow
                                } else {
                                    MaterialTheme.colorScheme.background
                                },
                                borderWidth = 2.dp,
                                shadowX = 3.dp,
                                shadowY = 3.dp
                            ) {
                                Text(
                                    text = "$preset MIN",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrutalActionButton(
                            title = if (running) "PAUSE" else "START",
                            background = BrutalColors.Ink,
                            modifier = Modifier.weight(1f),
                            onClick = { running = !running }
                        )
                        BrutalActionButton(
                            title = "RESET",
                            background = BrutalColors.Yellow,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                running = false
                                remainingSeconds = selectedMinutes * 60
                            }
                        )
                    }
                }
            }
        }

        item {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Lime,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "QUICK NOTE",
                        fontFamily = BrutalTypography.Display,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = noteDraft,
                            onValueChange = { noteDraft = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = BrutalColors.Ink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .border(2.dp, BrutalColors.Ink, RoundedCornerShape(0.dp))
                                .clickable {
                                    val cleaned = noteDraft.trim()
                                    if (cleaned.isNotEmpty()) {
                                        val updated = preferences.noteItems() +
                                            NeoListItem(text = cleaned)
                                        preferences.setNoteItems(updated)
                                        noteDraft = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        item {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Purple,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FOCUS APPS",
                        fontFamily = BrutalTypography.Display,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.8.sp,
                        color = BrutalColors.White
                    )
                    focusApps.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaunch(app) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%02d".format(Locale.ENGLISH, index + 1),
                                modifier = Modifier.width(28.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Orange
                            )
                            Text(
                                text = app.label.uppercase(),
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "→",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.White
                            )
                        }
                    }
                }
            }
        }

        item {
            NeoQuoteTile(
                quote = NeoQuotes.pairForToday().first,
                modifier = Modifier.fillMaxWidth().height(170.dp)
            )
        }
    }
}
