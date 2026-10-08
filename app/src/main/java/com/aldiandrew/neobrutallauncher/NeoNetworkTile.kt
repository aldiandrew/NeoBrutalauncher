package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Public
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

data class NeoNetworkState(val primary: String, val secondary: String)

private fun readNetworkState(context: Context): NeoNetworkState {
    val manager = context.getSystemService(ConnectivityManager::class.java)
    val network = manager.activeNetwork ?: return NeoNetworkState("OFFLINE", "NO LINK")
    val capabilities = manager.getNetworkCapabilities(network)
        ?: return NeoNetworkState("OFFLINE", "NO LINK")

    val primary = when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "MOBILE"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETH"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
        else -> "NETWORK"
    }
    val secondary = if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
        "CONNECTED"
    } else {
        "LIMITED"
    }
    return NeoNetworkState(primary, secondary)
}

@Composable
fun NeoNetworkTile(context: Context, modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(readNetworkState(context)) }

    DisposableEffect(context) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            private fun update() {
                val updated = readNetworkState(context)
                if (updated != state) {
                    state = updated
                }
            }

            override fun onAvailable(network: Network) {
                update()
            }
            override fun onLost(network: Network) {
                update()
            }
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                update()
            }
        }
        runCatching { manager.registerDefaultNetworkCallback(callback) }
        onDispose { runCatching { manager.unregisterNetworkCallback(callback) } }
    }

    val isDark = MaterialTheme.colorScheme.background == BrutalColors.DarkPaper
    val textColor = if (isDark) BrutalColors.DarkWhite else BrutalColors.Ink
    val icon = when (state.primary) {
        "WIFI" -> Icons.Default.Wifi
        "MOBILE" -> Icons.Default.SignalCellular4Bar
        else -> Icons.Default.Public
    }

    BrutalBlock(
        modifier = modifier,
        background = MaterialTheme.colorScheme.surface,
        borderWidth = 3.dp,
        borderColor = textColor,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = state.primary,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = state.primary,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = state.secondary,
                fontSize = 6.sp,
                lineHeight = 7.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
