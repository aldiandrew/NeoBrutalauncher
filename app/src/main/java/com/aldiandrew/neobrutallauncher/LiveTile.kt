package com.aldiandrew.neobrutallauncher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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

    LaunchedEffect(tileId, refreshIntervalMillis) {
        while (isActive) {
            loading = true

            try {
                value = loader()
                error = null
                lastUpdatedMillis = System.currentTimeMillis()
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                error = throwable
            } finally {
                loading = false
            }

            delay(refreshIntervalMillis)
        }
    }

    return LiveTileState(
        value = value,
        loading = loading,
        error = error,
        lastUpdatedMillis = lastUpdatedMillis
    )
}
