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

    fun appTileContentMode(): TileContentMode {
        return runCatching {
            TileContentMode.valueOf(
                prefs.getString(KEY_APP_TILE_CONTENT_MODE, TileContentMode.ICON_TEXT.name)
                    ?: TileContentMode.ICON_TEXT.name
            )
        }.getOrDefault(TileContentMode.ICON_TEXT)
    }

    fun setAppTileContentMode(value: TileContentMode) {
        prefs.edit()
            .putString(KEY_APP_TILE_CONTENT_MODE, value.name)
            .apply()
    }


    fun brutalityLevel(): BrutalityLevel {
        return runCatching {
            BrutalityLevel.valueOf(
                prefs.getString(KEY_BRUTALITY_LEVEL, BrutalityLevel.BRUTAL.name)
                    ?: BrutalityLevel.BRUTAL.name
            )
        }.getOrDefault(BrutalityLevel.BRUTAL)
    }

    fun setBrutalityLevel(value: BrutalityLevel) {
        prefs.edit().putString(KEY_BRUTALITY_LEVEL, value.name).apply()
    }

    fun cornerRadius(): Int {
        return prefs.getInt(KEY_CORNER_RADIUS, 0).coerceIn(0, 16)
    }

    fun setCornerRadius(value: Int) {
        prefs.edit().putInt(KEY_CORNER_RADIUS, value.coerceIn(0, 16)).apply()
    }

    fun clockStyle(): ClockStyle {
        return runCatching {
            ClockStyle.valueOf(
                prefs.getString(KEY_CLOCK_STYLE, ClockStyle.POSTER.name)
                    ?: ClockStyle.POSTER.name
            )
        }.getOrDefault(ClockStyle.POSTER)
    }

    fun setClockStyle(value: ClockStyle) {
        prefs.edit().putString(KEY_CLOCK_STYLE, value.name).apply()
    }

    fun wallpaperUri(): String? {
        return prefs.getString(KEY_WALLPAPER_URI, null)
    }

    fun setWallpaperUri(value: String?) {
        prefs.edit().apply {
            if (value == null) {
                remove(KEY_WALLPAPER_URI)
            } else {
                putString(KEY_WALLPAPER_URI, value)
            }
        }.apply()
    }

    fun chaosSeed(): Int {
        return prefs.getInt(KEY_CHAOS_SEED, 0)
    }

    fun setChaosSeed(value: Int) {
        prefs.edit().putInt(KEY_CHAOS_SEED, value).apply()
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
        private const val KEY_APP_TILE_CONTENT_MODE = "app_tile_content_mode"
        private const val KEY_BRUTALITY_LEVEL = "brutality_level"
        private const val KEY_CORNER_RADIUS = "corner_radius"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_CHAOS_SEED = "chaos_seed"
    }
}
