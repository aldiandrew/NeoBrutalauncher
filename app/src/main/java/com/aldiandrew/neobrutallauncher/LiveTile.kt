package com.aldiandrew.neobrutallauncher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Date

data class LiveTileState<T>(
    val value: T?,
    val loading: Boolean,
    val error: Throwable?,
    val lastUpdatedMillis: Long?
)

@Composable
fun <T> rememberLiveTileData(
    tileId: String,
    refreshIntervalMillis: Long,
    initialValue: T? = null,
    loader: suspend () -> T
): LiveTileState<T> {
    require(refreshIntervalMillis > 0L) {
        "Live tile refresh interval must be greater than zero"
    }

    var value by remember(tileId) {
        mutableStateOf(initialValue)
    }
    var loading by remember(tileId) {
        mutableStateOf(initialValue == null)
    }
    var error by remember(tileId) {
        mutableStateOf<Throwable?>(null)
    }
    var lastUpdatedMillis by remember(tileId) {
        mutableStateOf<Long?>(
            if (initialValue != null) {
                System.currentTimeMillis()
            } else {
                null
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val latestLoader by rememberUpdatedState(loader)

    LaunchedEffect(tileId, refreshIntervalMillis, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                val startedAtMillis = System.currentTimeMillis()
                loading = true

                try {
                    value = latestLoader()
                    error = null
                    lastUpdatedMillis = System.currentTimeMillis()
                } catch (cancellationException: CancellationException) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    error = throwable
                } finally {
                    loading = false
                }

                val elapsedMillis = System.currentTimeMillis() - startedAtMillis
                delay(
                    (refreshIntervalMillis - elapsedMillis)
                        .coerceAtLeast(0L)
                )
            }
        }
    }

    return LiveTileState(
        value = value,
        loading = loading,
        error = error,
        lastUpdatedMillis = lastUpdatedMillis
    )
}

@Composable
fun rememberMinuteClock(
    initialValue: Date = Date()
): Date {
    var value by remember {
        mutableStateOf(initialValue)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                val now = Date()
                value = now

                val millisToNextMinute =
                    60_000L - (now.time % 60_000L)

                delay(millisToNextMinute + 50L)
            }
        }
    }

    return value
}
