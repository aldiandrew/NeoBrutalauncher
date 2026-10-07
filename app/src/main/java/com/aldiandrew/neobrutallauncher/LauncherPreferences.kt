package com.aldiandrew.neobrutallauncher

import android.content.Context
import org.json.JSONArray

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

    fun homeAppOrder(): List<String> {
        val raw = prefs.getString(KEY_HOME_APP_ORDER, null)
        if (raw.isNullOrBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val key = array.optString(index).trim()
                    if (key.isNotEmpty()) add(key)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun setHomeAppOrder(values: List<String>) {
        val array = JSONArray()
        values.filter { it.isNotBlank() }.distinct().forEach(array::put)
        prefs.edit().putString(KEY_HOME_APP_ORDER, array.toString()).apply()
    }

    fun excludedHomeApps(): Set<String> {
        return prefs.getStringSet(KEY_EXCLUDED_HOME_APPS, emptySet())?.toSet().orEmpty()
    }

    fun setExcludedHomeApps(values: Set<String>) {
        prefs.edit().putStringSet(KEY_EXCLUDED_HOME_APPS, values.toSet()).apply()
    }

    fun appShortcutKey(): String? {
        return prefs.getString(KEY_APP_SHORTCUT, null)?.takeIf { it.isNotBlank() }
    }

    fun setAppShortcutKey(value: String?) {
        prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_APP_SHORTCUT) else putString(KEY_APP_SHORTCUT, value)
        }.apply()
    }

    fun appLaunchCounts(): Map<String, Int> {
        return prefs.getStringSet(KEY_APP_LAUNCH_COUNTS, emptySet())
            ?.mapNotNull { entry ->
                val index = entry.lastIndexOf("|")
                if (index <= 0) return@mapNotNull null
                val key = entry.substring(0, index)
                val count = entry.substring(index + 1).toIntOrNull()
                if (key.isBlank() || count == null || count < 1) null else key to count
            }
            ?.toMap()
            .orEmpty()
    }

    fun recordAppLaunch(key: String) {
        if (key.isBlank()) return
        val updated = appLaunchCounts().toMutableMap()
        updated[key] = (updated[key] ?: 0) + 1
        prefs.edit()
            .putStringSet(
                KEY_APP_LAUNCH_COUNTS,
                updated.map { (id, count) -> "$id|$count" }.toSet()
            )
            .apply()
    }

    fun noteItems(): List<NeoListItem> = readItems(KEY_NOTE_ITEMS, KEY_NOTE_TEXT)

    fun setNoteItems(values: List<NeoListItem>) {
        writeItems(KEY_NOTE_ITEMS, values)
    }

    fun taskItems(): List<NeoListItem> = readItems(KEY_TASK_ITEMS, KEY_TASK_TEXT)

    fun setTaskItems(values: List<NeoListItem>) {
        writeItems(KEY_TASK_ITEMS, values)
    }

    private fun readItems(key: String, legacyKey: String): List<NeoListItem> {
        val raw = prefs.getString(key, null)

        if (!raw.isNullOrBlank()) {
            return runCatching {
                val array = JSONArray(raw)
                buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val text = item.optString("text").trim()
                        if (text.isNotEmpty()) {
                            add(
                                NeoListItem(
                                    text = text,
                                    checked = item.optBoolean("checked", false)
                                )
                            )
                        }
                    }
                }
            }.getOrDefault(emptyList())
        }

        val legacy = prefs.getString(legacyKey, "")?.trim().orEmpty()
        return if (legacy.isBlank()) emptyList() else listOf(NeoListItem(legacy))
    }

    private fun writeItems(key: String, values: List<NeoListItem>) {
        val array = JSONArray()
        values.filter { it.text.isNotBlank() }.forEach { item ->
            array.put(
                org.json.JSONObject()
                    .put("text", item.text)
                    .put("checked", item.checked)
            )
        }
        prefs.edit().putString(key, array.toString()).apply()
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
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_CHAOS_SEED = "chaos_seed"
        private const val KEY_NOTE_TEXT = "note_text"
        private const val KEY_NOTE_ITEMS = "note_items"
        private const val KEY_TASK_ITEMS = "task_items"
        private const val KEY_APP_LAUNCH_COUNTS = "app_launch_counts"
        private const val KEY_HOME_APP_ORDER = "home_app_order"
        private const val KEY_EXCLUDED_HOME_APPS = "excluded_home_apps"
        private const val KEY_APP_SHORTCUT = "app_shortcut"
        private const val KEY_TASK_TEXT = "task_text"
    }
}
