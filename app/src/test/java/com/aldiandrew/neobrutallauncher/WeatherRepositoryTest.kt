package com.aldiandrew.neobrutallauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun mapsClearCloudyFogAndDrizzleCodes() {
        assertEquals("Clear", WeatherRepository.weatherDescription(0))
        listOf(1, 2, 3).forEach {
            assertEquals("Cloudy", WeatherRepository.weatherDescription(it))
        }
        listOf(45, 48).forEach {
            assertEquals("Fog", WeatherRepository.weatherDescription(it))
        }
        listOf(51, 53, 55, 56, 57).forEach {
            assertEquals("Drizzle", WeatherRepository.weatherDescription(it))
        }
    }

    @Test
    fun mapsRainSnowAndShowersCodes() {
        listOf(61, 63, 65, 66, 67).forEach {
            assertEquals("Rain", WeatherRepository.weatherDescription(it))
        }
        listOf(71, 73, 75, 77, 85, 86).forEach {
            assertEquals("Snow", WeatherRepository.weatherDescription(it))
        }
        listOf(80, 81, 82).forEach {
            assertEquals("Showers", WeatherRepository.weatherDescription(it))
        }
    }

    @Test
    fun mapsThunderstormAndUnknownCodes() {
        listOf(95, 96, 99).forEach {
            assertEquals("Thunderstorm", WeatherRepository.weatherDescription(it))
        }
        listOf(-1, 4, 50, 100, Int.MIN_VALUE).forEach {
            assertEquals("Unknown", WeatherRepository.weatherDescription(it))
        }
    }
}
