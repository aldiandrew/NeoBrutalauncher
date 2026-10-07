package com.aldiandrew.neobrutallauncher

import android.content.Context

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

class LauncherPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(
        "neo_brutal_launcher_preferences",
        Context.MODE_PRIVATE
    )

    fun theme(): ThemePreference {
        return when (prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name)) {
            ThemePreference.LIGHT.name -> ThemePreference.LIGHT
            ThemePreference.DARK.name -> ThemePreference.DARK
            else -> ThemePreference.SYSTEM
        }
    }

    fun setTheme(value: ThemePreference) {
        prefs.edit().putString(KEY_THEME, value.name).apply()
    }

    fun use24Hour(): Boolean {
        return prefs.getBoolean(KEY_24_HOUR, true)
    }

    fun setUse24Hour(value: Boolean) {
        prefs.edit().putBoolean(KEY_24_HOUR, value).apply()
    }

    fun showDate(): Boolean {
        return prefs.getBoolean(KEY_DATE, true)
    }

    fun setShowDate(value: Boolean) {
        prefs.edit().putBoolean(KEY_DATE, value).apply()
    }

    fun homeAppCount(): Int {
        return prefs.getInt(KEY_HOME_APP_COUNT, 6).coerceIn(2, 8)
    }

    fun setHomeAppCount(value: Int) {
        prefs.edit().putInt(KEY_HOME_APP_COUNT, value.coerceIn(2, 8)).apply()
    }

    fun showTagline(): Boolean {
        return prefs.getBoolean(KEY_TAGLINE, true)
    }

    fun setShowTagline(value: Boolean) {
        prefs.edit().putBoolean(KEY_TAGLINE, value).apply()
    }

    fun showAppCount(): Boolean {
        return prefs.getBoolean(KEY_APP_COUNT, true)
    }

    fun setShowAppCount(value: Boolean) {
        prefs.edit().putBoolean(KEY_APP_COUNT, value).apply()
    }

    fun showWeather(): Boolean {
        return prefs.getBoolean(KEY_WEATHER, false)
    }

    fun setShowWeather(value: Boolean) {
        prefs.edit().putBoolean(KEY_WEATHER, value).apply()
    }

    fun showQuote(): Boolean {
        return prefs.getBoolean(KEY_QUOTE, false)
    }

    fun setShowQuote(value: Boolean) {
        prefs.edit().putBoolean(KEY_QUOTE, value).apply()
    }

    fun showBattery(): Boolean {
        return prefs.getBoolean(KEY_BATTERY, false)
    }

    fun setShowBattery(value: Boolean) {
        prefs.edit().putBoolean(KEY_BATTERY, value).apply()
    }

    fun favorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet().orEmpty()
    }

    fun setFavorites(values: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, values.toSet()).apply()
    }

    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }

    fun tilePositions(): Map<String, NeoTilePosition> {
        return prefs.getStringSet(KEY_TILE_POSITIONS, emptySet())
            ?.mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size != 3) {
                    return@mapNotNull null
                }

                val tileId = parts[0]
                val column = parts[1].toIntOrNull()
                val row = parts[2].toIntOrNull()

                if (
                    tileId.isBlank() ||
                    column == null ||
                    row == null ||
                    column < 0 ||
                    row < 0
                ) {
                    null
                } else {
                    tileId to NeoTilePosition(
                        column = column,
                        row = row
                    )
                }
            }
            ?.toMap()
            .orEmpty()
    }

    fun setTilePositions(values: Map<String, NeoTilePosition>) {
        val encoded = values.mapNotNull { (tileId, position) ->
            if (
                tileId.isBlank() ||
                position.column < 0 ||
                position.row < 0
            ) {
                null
            } else {
                tileId + "|" + position.column + "|" + position.row
            }
        }.toSet()

        prefs.edit()
            .putStringSet(KEY_TILE_POSITIONS, encoded)
            .apply()
    }

    fun tileSizes(): Map<String, NeoTileSize> {
        return prefs.getStringSet(KEY_TILE_SIZES, emptySet())
            ?.mapNotNull { entry ->
                val index = entry.lastIndexOf("|")
                if (index <= 0) {
                    return@mapNotNull null
                }

                val tileId = entry.substring(0, index)
                val size = runCatching {
                    NeoTileSize.valueOf(entry.substring(index + 1))
                }.getOrNull()

                if (tileId.isBlank() || size == null) {
                    null
                } else {
                    tileId to size
                }
            }
            ?.toMap()
            .orEmpty()
    }

    fun setTileSizes(values: Map<String, NeoTileSize>) {
        val encoded = values.mapNotNull { (tileId, size) ->
            if (tileId.isBlank()) {
                null
            } else {
                tileId + "|" + size.name
            }
        }.toSet()

        prefs.edit()
            .putStringSet(KEY_TILE_SIZES, encoded)
            .apply()
    }

    fun tileContentModes(): Map<String, TileContentMode> {
        return prefs.getStringSet(KEY_TILE_CONTENT_MODES, emptySet())
            ?.mapNotNull { entry ->
                val index = entry.lastIndexOf("|")
                if (index <= 0) {
                    return@mapNotNull null
                }

                val tileId = entry.substring(0, index)
                val mode = runCatching {
                    TileContentMode.valueOf(entry.substring(index + 1))
                }.getOrNull()

                if (tileId.isBlank() || mode == null) null else tileId to mode
            }
            ?.toMap()
            .orEmpty()
    }

    fun setTileContentModes(values: Map<String, TileContentMode>) {
        val encoded = values.mapNotNull { (tileId, mode) ->
            if (tileId.isBlank()) null else tileId + "|" + mode.name
        }.toSet()

        prefs.edit()
            .putStringSet(KEY_TILE_CONTENT_MODES, encoded)
            .apply()
    }

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_24_HOUR = "use_24_hour"
        private const val KEY_DATE = "show_date"
        private const val KEY_HOME_APP_COUNT = "home_app_count"
        private const val KEY_TAGLINE = "show_tagline"
        private const val KEY_APP_COUNT = "show_app_count"
        private const val KEY_WEATHER = "show_weather"
        private const val KEY_QUOTE = "show_quote"
        private const val KEY_BATTERY = "show_battery"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_TILE_POSITIONS = "tile_positions"
        private const val KEY_TILE_SIZES = "tile_sizes"
        private const val KEY_TILE_CONTENT_MODES = "tile_content_modes"
    }
}
