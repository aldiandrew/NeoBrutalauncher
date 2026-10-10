package com.aldiandrew.neobrutallauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat

data class BatteryTileState(
    val percentage: Int,
    val charging: Boolean
)

@Composable
fun rememberBatteryTileState(context: Context): BatteryTileState {
    var state by remember { mutableStateOf(readBatteryState(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                state = readBatteryState(intent)
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    return state
}

private fun readBatteryState(context: Context): BatteryTileState =
    readBatteryState(
        context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    )

private fun readBatteryState(intent: Intent?): BatteryTileState {
    if (intent == null) return BatteryTileState(0, false)

    val level = intent.getIntExtra("level", 0)
    val scale = intent.getIntExtra("scale", 100)
    val status = intent.getIntExtra("status", 0)

    return BatteryTileState(
        percentage = if (scale > 0) (level * 100 / scale).coerceIn(0, 100) else 0,
        charging = status == 2 || status == 5
    )
}

@Composable
fun BatteryTile(
    context: Context,
    modifier: Modifier = Modifier,
    background: Color
) {
    val battery = rememberBatteryTileState(context)

    BrutalBlock(
        modifier = modifier,
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = minOf(maxWidth, maxHeight)

            if (compact < 78.dp) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (battery.charging) {
                            Icons.Default.BatteryChargingFull
                        } else {
                            Icons.Default.BatteryFull
                        },
                        contentDescription = if (battery.charging) "Charging" else "Battery",
                        tint = BrutalColors.Ink,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "${battery.percentage}%",
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                    Text(
                        text = if (battery.charging) "CHG" else "BAT",
                        fontSize = 6.sp,
                        lineHeight = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 1
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "BATTERY",
                        fontSize = if (compact < 155.dp) 9.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = battery.percentage.toString(),
                            fontSize = if (compact < 155.dp) 32.sp else 42.sp,
                            lineHeight = if (compact < 155.dp) 33.sp else 43.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "%",
                            fontSize = if (compact < 155.dp) 15.sp else 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (battery.charging) "CHARGING" else "ON BATTERY",
                        fontSize = if (compact < 155.dp) 8.sp else 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
