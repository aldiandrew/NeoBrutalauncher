package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NeoWeatherTile(
    context: android.content.Context,
    refreshToken: Int,
    modifier: Modifier = Modifier
) {
    var weather by remember { mutableStateOf<WeatherData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    var lastUpdatedMillis by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(refreshToken) {
        loading = true
        error = null

        runCatching {
            WeatherRepository.loadCurrentWeather(context)
        }.onSuccess {
            weather = it
            lastUpdatedMillis = System.currentTimeMillis()
        }.onFailure {
            error = it
        }

        loading = false
    }

    val icon = when (weather?.weatherCode) {
        0 -> Icons.Default.WbSunny
        1, 2, 3, 45, 48 -> Icons.Default.Cloud
        51, 53, 55, 56, 57 -> Icons.Default.Opacity
        61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Default.Umbrella
        71, 73, 75, 77, 85, 86 -> Icons.Default.AcUnit
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }

    val currentWeather = weather

    BrutalBlock(
        modifier = modifier,
        background = BrutalColors.Cyan,
        borderWidth = 4.dp,
        shadowX = 8.dp,
        shadowY = 8.dp
    ) {
        if (currentWeather != null) {
            val updatedText = lastUpdatedMillis?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
            } ?: "--:--"

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WEATHER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = BrutalColors.Ink
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = currentWeather.description.uppercase(),
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${currentWeather.temperatureC.toInt()}°",
                        fontSize = 48.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        WeatherMetric("HUMIDITY", "${currentWeather.humidityPercent}%")
                        WeatherMetric("WIND", "${currentWeather.windKph.toInt()} KM/H")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "UPDATED $updatedText  •  TAP TO REFRESH",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink.copy(alpha = 0.72f),
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = weather.description,
                        tint = BrutalColors.Ink,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when {
                        loading -> "LOADING WEATHER..."
                        error != null -> "WEATHER ERROR"
                        else -> "WEATHER UNAVAILABLE"
                    },
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "TAP WEATHER TILE TO TRY AGAIN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
            }
        }
    }
}

@Composable
private fun WeatherMetric(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = BrutalColors.Ink.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = BrutalColors.Ink
        )
    }
}
