package com.aldiandrew.neobrutallauncher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.Geocoder
import android.location.LocationManager
import android.location.LocationListener
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

data class WeatherData(
    val locationName: String,
    val temperatureC: Double,
    val weatherCode: Int,
    val description: String,
    val humidityPercent: Int,
    val windKph: Double
)

object WeatherRepository {

    @Volatile
    private var cachedWeather: WeatherData? = null

    fun cachedWeather(context: Context): WeatherData? {
        cachedWeather?.let { return it }
        val prefs = context.applicationContext.getSharedPreferences("neo_weather_cache", Context.MODE_PRIVATE)
        val temperature = prefs.getString("temperature", null)?.toDoubleOrNull() ?: return null
        val code = prefs.getInt("code", Int.MIN_VALUE)
        if (code == Int.MIN_VALUE) return null
        return WeatherData(
            locationName = prefs.getString("location", "LAST KNOWN LOCATION") ?: "LAST KNOWN LOCATION",
            temperatureC = temperature,
            weatherCode = code,
            description = weatherDescription(code),
            humidityPercent = prefs.getInt("humidity", 0),
            windKph = prefs.getFloat("wind_kph", 0f).toDouble()
        ).also { cachedWeather = it }
    }

    @SuppressLint("MissingPermission")
    suspend fun loadCurrentWeather(context: Context): WeatherData {
        if (
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            return cachedWeather(context)
                ?: throw IllegalStateException("Location permission is required")
        }

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val location = getLocation(locationManager, context)
            ?: return cachedWeather(context)
                ?: throw IllegalStateException("No location or saved weather data is available")

        val locationName = resolveLocationName(context, location)
        return fetchWeather(context, location.latitude, location.longitude, locationName)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(
        locationManager: LocationManager,
        context: Context
    ): Location? {
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
        }

        val enabledProviders = providers.filter { provider ->
            runCatching {
                locationManager.isProviderEnabled(provider)
            }.getOrDefault(false)
        }

        val lastKnown = (providers + LocationManager.PASSIVE_PROVIDER)
            .distinct()
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
            for (provider in enabledProviders) {
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
        } else {
            for (provider in enabledProviders) {
                val current = withTimeoutOrNull(8_000L) {
                    suspendCancellableCoroutine<Location?> { continuation ->
                        val listener = object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                if (continuation.isActive) {
                                    runCatching {
                                        locationManager.removeUpdates(this)
                                    }
                                    continuation.resume(location)
                                }
                            }
                        }

                        continuation.invokeOnCancellation {
                            runCatching {
                                locationManager.removeUpdates(listener)
                            }
                        }

                        runCatching {
                            locationManager.requestLocationUpdates(
                                provider,
                                0L,
                                0f,
                                listener,
                                Looper.getMainLooper()
                            )
                        }.onFailure {
                            if (continuation.isActive) {
                                continuation.resume(null)
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

    @Suppress("DEPRECATION")
    private suspend fun resolveLocationName(
        context: Context,
        location: Location
    ): String = withContext(Dispatchers.IO) {
        runCatching {
            if (!Geocoder.isPresent()) return@runCatching null
            val geocoder = Geocoder(context, java.util.Locale.getDefault())
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.let { address ->
                    listOfNotNull(
                        address.locality,
                        address.subAdminArea,
                        address.adminArea
                    )
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .joinToString(", ")
                        .takeIf { it.isNotEmpty() }
                }
        }.getOrNull() ?: "CURRENT LOCATION"
    }

    private suspend fun fetchWeather(
        context: Context,
        latitude: Double,
        longitude: Double,
        locationName: String
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
                locationName = locationName,
                temperatureC = temperature,
                weatherCode = weatherCode,
                description = weatherDescription(weatherCode),
                humidityPercent = humidity,
                windKph = windKph
            ).also { data ->
                cachedWeather = data
                context.applicationContext.getSharedPreferences("neo_weather_cache", Context.MODE_PRIVATE)
                    .edit()
                    .putString("location", data.locationName)
                    .putString("temperature", data.temperatureC.toString())
                    .putInt("code", data.weatherCode)
                    .putInt("humidity", data.humidityPercent)
                    .putFloat("wind_kph", data.windKph.toFloat())
                    .apply()
            }
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
