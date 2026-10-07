package com.aldiandrew.neobrutallauncher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

data class WeatherData(
    val temperatureC: Double,
    val weatherCode: Int,
    val description: String,
    val humidityPercent: Int,
    val windKph: Double
)

object WeatherRepository {

    @Volatile
    private var cachedWeather: WeatherData? = null

    fun cachedWeather(): WeatherData? = cachedWeather

    @SuppressLint("MissingPermission")
    suspend fun loadCurrentWeather(context: Context): WeatherData {
        if (
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            throw IllegalStateException("Location permission is required")
        }

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val location = getLocation(locationManager, context)
            ?: throw IllegalStateException("Unable to determine current location")

        return fetchWeather(location.latitude, location.longitude)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(
        locationManager: LocationManager,
        context: Context
    ): Location? {
        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        val lastKnown = providers
            .filter { provider ->
                runCatching {
                    provider == LocationManager.PASSIVE_PROVIDER ||
                        locationManager.isProviderEnabled(provider)
                }.getOrDefault(false)
            }
            .mapNotNull { provider ->
                runCatching {
                    locationManager.getLastKnownLocation(provider)
                }.getOrNull()
            }
            .maxByOrNull { it.time }

        if (lastKnown != null) {
            return lastKnown
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val provider = providers
                .filter { it != LocationManager.PASSIVE_PROVIDER }
                .firstOrNull { candidate ->
                    runCatching {
                        locationManager.isProviderEnabled(candidate)
                    }.getOrDefault(false)
                }

            if (provider != null) {
                val current = withTimeoutOrNull(8_000L) {
                    suspendCancellableCoroutine<Location?> { continuation ->
                        val signal = CancellationSignal()

                        continuation.invokeOnCancellation {
                            signal.cancel()
                        }

                        locationManager.getCurrentLocation(
                            provider,
                            signal,
                            context.mainExecutor
                        ) { location ->
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                        }
                    }
                }

                if (current != null) {
                    return current
                }
            }
        }

        return null
    }

    private suspend fun fetchWeather(
        latitude: Double,
        longitude: Double
    ): WeatherData = withContext(Dispatchers.IO) {
        val endpoint =
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude" +
                "&longitude=$longitude" +
                "&current=temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m" +
                "&timezone=auto"

        val connection =
            URL(endpoint).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                throw IllegalStateException(
                    "Weather service returned HTTP $responseCode"
                )
            }

            val response =
                connection.inputStream.bufferedReader().use { it.readText() }

            val current = JSONObject(response).getJSONObject("current")
            val temperature = current.getDouble("temperature_2m")
            val weatherCode = current.getInt("weather_code")
            val humidity = current.getInt("relative_humidity_2m")
            val windKph = current.getDouble("wind_speed_10m")

            WeatherData(
                temperatureC = temperature,
                weatherCode = weatherCode,
                description = weatherDescription(weatherCode),
                humidityPercent = humidity,
                windKph = windKph
            ).also { cachedWeather = it }
        } finally {
            connection.disconnect()
        }
    }

    private fun weatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67 -> "Rain"
            71, 73, 75, 77, 85, 86 -> "Snow"
            80, 81, 82 -> "Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }
}
