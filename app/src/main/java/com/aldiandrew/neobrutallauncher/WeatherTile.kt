package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
    var weather by remember {
        mutableStateOf(WeatherRepository.cachedWeather())
    }
    var loading by remember { mutableStateOf(weather == null) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    val locationGranted =
        context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    var lastUpdatedMillis by remember {
        mutableStateOf(if (weather != null) System.currentTimeMillis() else null)
    }

    LaunchedEffect(refreshToken, locationGranted) {
        val cached = WeatherRepository.cachedWeather()

        if (!locationGranted) {
            weather = null
            loading = false
            error = null
            return@LaunchedEffect
        }

        if (refreshToken == 0 && cached != null) {
            weather = cached
            loading = false
            error = null
            if (lastUpdatedMillis == null) {
                lastUpdatedMillis = System.currentTimeMillis()
            }
            return@LaunchedEffect
        }

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

    val currentWeather = weather
    val updatedTimeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val icon = when (currentWeather?.weatherCode) {
        0 -> Icons.Default.WbSunny
        1, 2, 3, 45, 48 -> Icons.Default.Cloud
        51, 53, 55, 56, 57 -> Icons.Default.Opacity
        61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Default.Umbrella
        71, 73, 75, 77, 85, 86 -> Icons.Default.AcUnit
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }

    BrutalBlock(
        modifier = modifier,
        background = BrutalColors.Cyan,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val compact = minOf(maxWidth, maxHeight)

            if (currentWeather != null) {
                val updatedText = lastUpdatedMillis?.let {
                    updatedTimeFormatter.format(Date(it))
                } ?: "--:--"

                when {
                    compact < 78.dp -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${currentWeather!!.temperatureC.toInt()}°",
                                fontSize = 18.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = icon,
                                contentDescription = currentWeather!!.description,
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.width(28.dp).height(28.dp)
                            )
                            Text(
                                text = "${currentWeather!!.humidityPercent}% • ${currentWeather!!.windKph.toInt()}K",
                                fontSize = 7.sp,
                                lineHeight = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = 1
                            )
                        }
                    }

                    compact < 155.dp -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(9.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentWeather!!.description.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = currentWeather!!.description,
                                    tint = BrutalColors.Ink,
                                    modifier = Modifier.width(48.dp).height(48.dp)
                                )
                                Text(
                                    text = "${currentWeather!!.temperatureC.toInt()}°",
                                    fontSize = 31.sp,
                                    lineHeight = 31.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "H ${currentWeather!!.humidityPercent}%   W ${currentWeather!!.windKph.toInt()}K",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = 1
                            )
                        }
                    }

                    else -> {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(14.dp),
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
                                    text = currentWeather!!.description.uppercase(),
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${currentWeather!!.temperatureC.toInt()}°",
                                    fontSize = 48.sp,
                                    lineHeight = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(5.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    WeatherMetric("HUMIDITY", "${currentWeather!!.humidityPercent}%")
                                    WeatherMetric("WIND", "${currentWeather!!.windKph.toInt()} KM/H")
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "UPDATED $updatedText • TAP TO REFRESH",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrutalColors.Ink.copy(alpha = 0.72f),
                                    maxLines = 1
                                )
                            }

                            Box(
                                modifier = Modifier.width(88.dp).height(88.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = currentWeather!!.description,
                                    tint = BrutalColors.Ink,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WEATHER",
                        fontSize = if (compact < 100.dp) 12.sp else 20.sp,
                        lineHeight = if (compact < 100.dp) 13.sp else 22.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = when {
                            !locationGranted -> "ALLOW LOCATION IN SETTINGS"
                            loading -> "LOADING..."
                            error != null -> "TAP TO RETRY"
                            else -> if (compact < 100.dp) "TAP" else "TAP TO REFRESH"
                        },
                        fontSize = if (compact < 100.dp) 8.sp else 10.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                }
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
