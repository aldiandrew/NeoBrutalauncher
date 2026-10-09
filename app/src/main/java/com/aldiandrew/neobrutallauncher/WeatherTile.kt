package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            return@LaunchedEffect
        }

        loading = true
        error = null

        runCatching {
            WeatherRepository.loadCurrentWeather(context)
        }.onSuccess {
            weather = it
        }.onFailure {
            error = it
        }

        loading = false
    }

    val currentWeather = weather
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
                val locationSize = when {
                    compact < 90.dp -> 8.sp
                    compact < 155.dp -> 10.sp
                    else -> 12.sp
                }
                val temperatureSize = when {
                    compact < 90.dp -> 26.sp
                    compact < 155.dp -> 34.sp
                    else -> 48.sp
                }
                val conditionSize = when {
                    compact < 90.dp -> 8.sp
                    compact < 155.dp -> 10.sp
                    else -> 12.sp
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (compact < 100.dp) 4.dp else 6.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = currentWeather.description,
                        tint = BrutalColors.Ink,
                        modifier = Modifier.size(
                            when {
                                compact < 90.dp -> 22.dp
                                compact < 155.dp -> 30.dp
                                else -> 36.dp
                            }
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = currentWeather.locationName.uppercase(Locale.ENGLISH),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = if (compact < 90.dp) 7.sp else 9.sp,
                        lineHeight = if (compact < 90.dp) 8.sp else 10.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = currentWeather.temperatureC.toInt().toString() + "°",
                        fontSize = if (compact < 90.dp) 20.sp else 26.sp,
                        lineHeight = if (compact < 90.dp) 21.sp else 27.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WEATHER",
                        fontSize = if (compact < 100.dp) 12.sp else 20.sp,
                        lineHeight = if (compact < 100.dp) 13.sp else 22.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = when {
                            !locationGranted -> "ALLOW LOCATION IN SETTINGS"
                            loading -> "LOADING..."
                            error != null -> "TAP TO RETRY"
                            else -> "TAP TO REFRESH"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = if (compact < 100.dp) 8.sp else 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
