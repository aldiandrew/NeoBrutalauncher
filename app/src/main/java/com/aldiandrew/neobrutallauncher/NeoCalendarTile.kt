package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun NeoCalendarTile(
    context: Context,
    modifier: Modifier = Modifier,
    background: Color = BrutalColors.Cyan,
    textColor: Color = BrutalColors.Ink
) {
    val today = remember { Calendar.getInstance() }
    var displayedMonth by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }
        )
    }

    val monthTitle = remember(displayedMonth.timeInMillis) {
        SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            .format(displayedMonth.time)
            .uppercase(Locale.ENGLISH)
    }

    val daysInMonth = displayedMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = displayedMonth.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY

    val openCalendar: () -> Unit = {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_APP_CALENDAR)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (context.packageManager.resolveActivity(intent, 0) != null) {
            context.startActivity(intent)
        } else {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
        }
        Unit
    }

    BrutalBlock(
        modifier = modifier.clickable(onClick = openCalendar),
        background = background,
        borderWidth = 4.dp,
        borderColor = if (background == BrutalColors.DarkTile) {
            BrutalColors.DarkWhite
        } else {
            BrutalColors.Ink
        },
        shadowX = 6.dp,
        shadowY = 6.dp,
        shadowColor = if (background == BrutalColors.DarkTile) {
            BrutalColors.DarkWhite
        } else {
            BrutalColors.Ink
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrutalLabel(
                    text = "CALENDAR",
                    background = BrutalColors.Yellow
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = monthTitle,
                    modifier = Modifier.weight(1f),
                    fontFamily = BrutalTypography.Display,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.3.sp,
                    color = textColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalendarNavButton(
                        label = "‹",
                        onClick = {
                            displayedMonth = (displayedMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        }
                    )
                    CalendarNavButton(
                        label = "›",
                        onClick = {
                            displayedMonth = (displayedMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                }
            }

            Column(
                modifier = Modifier.height(107.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                for (week in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val cellIndex = week * 7 + dayOfWeek
                            val dayNumber = cellIndex - firstDayOffset + 1
                            val inMonth = dayNumber in 1..daysInMonth
                            val isToday = inMonth &&
                                today.get(Calendar.YEAR) == displayedMonth.get(Calendar.YEAR) &&
                                today.get(Calendar.MONTH) == displayedMonth.get(Calendar.MONTH) &&
                                today.get(Calendar.DAY_OF_MONTH) == dayNumber

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(17.dp)
                                    .then(
                                        if (isToday) {
                                            Modifier
                                                .background(BrutalColors.Yellow)
                                                .border(2.dp, BrutalColors.Ink)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (inMonth) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = if (isToday) {
                                            FontWeight.Black
                                        } else {
                                            FontWeight.Bold
                                        },
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "TODAY  /  " +
                    SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH).format(today.time).uppercase(Locale.ENGLISH),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start,
                color = textColor,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CalendarNavButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(BrutalColors.White)
            .border(2.dp, BrutalColors.Ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = BrutalColors.Ink
        )
    }
}
