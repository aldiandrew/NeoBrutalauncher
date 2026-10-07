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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun NeoWeatherTile(
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val weatherLive = rememberLiveTileData(
        tileId = "weather",
        refreshIntervalMillis = 15 * 60 * 1000L,
        loader = { WeatherRepository.loadCurrentWeather(context) }
    )

    val weather = weatherLive.value
    val icon = when (weather?.weatherCode) {
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
        borderWidth = 4.dp,
        shadowX = 8.dp,
        shadowY = 8.dp
    ) {
        if (weather != null) {
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
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = weather.description.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${weather.temperatureC.toInt()}°C",
                        fontSize = 48.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        WeatherMetric("HUMIDITY", "${weather.humidityPercent}%")
                        WeatherMetric("WIND", "${weather.windKph.toInt()} KM/H")
                    }
                }

                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .height(92.dp),
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
                        weatherLive.loading -> "LOADING WEATHER..."
                        weatherLive.error != null -> "ALLOW LOCATION + NETWORK"
                        else -> "WEATHER UNAVAILABLE"
                    },
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "OPEN-METEO / 15 MIN REFRESH",
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
