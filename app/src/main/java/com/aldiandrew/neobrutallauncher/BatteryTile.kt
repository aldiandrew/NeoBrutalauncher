package com.aldiandrew.neobrutallauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

data class BatteryTileState(
    val percentage: Int,
    val charging: Boolean
)

@Composable
fun rememberBatteryTileState(context: Context): BatteryTileState {
    var state by remember {
        mutableStateOf(readBatteryState(context))
    }

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
            runCatching {
                context.unregisterReceiver(receiver)
            }
        }
    }

    return state
}

private fun readBatteryState(context: Context): BatteryTileState {
    return readBatteryState(
        context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    )
}

private fun readBatteryState(intent: Intent?): BatteryTileState {
    if (intent == null) {
        return BatteryTileState(
            percentage = 0,
            charging = false
        )
    }

    val level = intent.getIntExtra("level", 0)
    val scale = intent.getIntExtra("scale", 100)
    val status = intent.getIntExtra("status", 0)

    val percentage = if (scale > 0) {
        (level * 100 / scale).coerceIn(0, 100)
    } else {
        0
    }

    return BatteryTileState(
        percentage = percentage,
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
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "BATTERY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = BrutalColors.Ink
            )

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = battery.percentage.toString(),
                    fontSize = 42.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
            }

            Text(
                text = if (battery.charging) "CHARGING" else "ON BATTERY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = BrutalColors.Ink
            )
        }
    }
}
