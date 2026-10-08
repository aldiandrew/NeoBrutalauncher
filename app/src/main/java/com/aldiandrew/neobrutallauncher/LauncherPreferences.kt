package com.aldiandrew.neobrutallauncher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

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

    fun showAmPm(): Boolean {
        return prefs.getBoolean(KEY_SHOW_AM_PM, true)
    }

    fun setShowAmPm(value: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_AM_PM, value).apply()
    }

    fun homeAppCount(): Int {
        return normalizePinnedCount(prefs.getInt(KEY_HOME_APP_COUNT, 5))
    }

    fun homeAppsInitialized(): Boolean = prefs.getBoolean(KEY_HOME_APPS_INITIALIZED, false)

    fun setHomeAppsInitialized(value: Boolean) {
        prefs.edit().putBoolean(KEY_HOME_APPS_INITIALIZED, value).apply()
    }

    fun onboardingCompleted(): Boolean {
        if (prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)) return true

        // Existing installations already have launcher preferences, so do not interrupt
        // an upgrade with first-run onboarding.
        return prefs.contains(KEY_FAVORITES) ||
            prefs.contains(KEY_HOME_APP_COUNT) ||
            prefs.contains(KEY_HOME_APPS_INITIALIZED)
    }

    fun setOnboardingCompleted(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    }

    fun lastMusicPackage(): String? =
        prefs.getString(KEY_LAST_MUSIC_PACKAGE, null)?.takeIf { it.isNotBlank() }

    fun setLastMusicPackage(value: String?) {
        prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_LAST_MUSIC_PACKAGE) else putString(KEY_LAST_MUSIC_PACKAGE, value)
        }.apply()
    }

    fun setHomeAppCount(value: Int) {
        prefs.edit().putInt(KEY_HOME_APP_COUNT, normalizePinnedCount(value)).apply()
    }

    private fun normalizePinnedCount(value: Int): Int {
        return when (value) {
            in Int.MIN_VALUE..3 -> 3
            in 4..6 -> 5
            else -> 7
        }
    }

    fun showWeather(): Boolean {
        return prefs.getBoolean(KEY_WEATHER, false)
    }

    fun setShowWeather(value: Boolean) {
        prefs.edit().putBoolean(KEY_WEATHER, value).apply()
    }

    fun chatNotificationPackages(): List<String> =
        prefs.getStringSet(KEY_CHAT_NOTIFICATION_PACKAGES, emptySet())
            ?.toList()
            .orEmpty()
            .filter { it.isNotBlank() }
            .take(1)

    fun setChatNotificationPackages(values: List<String>) {
        prefs.edit()
            .putStringSet(KEY_CHAT_NOTIFICATION_PACKAGES, values.filter { it.isNotBlank() }.distinct().take(1).toSet())
            .apply()
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

    fun iconPackPackage(): String? =
        prefs.getString(KEY_ICON_PACK_PACKAGE, null)?.takeIf { it.isNotBlank() }

    fun setIconPackPackage(value: String?) {
        prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_ICON_PACK_PACKAGE) else putString(KEY_ICON_PACK_PACKAGE, value)
        }.apply()
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


    fun typographyStyle(): TypographyStyle {
        return when (prefs.getString(KEY_CLOCK_STYLE, TypographyStyle.DEFAULT.name)) {
            TypographyStyle.CONDENSED.name -> TypographyStyle.CONDENSED
            else -> TypographyStyle.DEFAULT
        }
    }

    fun setTypographyStyle(value: TypographyStyle) {
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

    fun noteItems(): List<NeoListItem> = readItems(KEY_NOTE_ITEMS, KEY_NOTE_TEXT)

    fun setNoteItems(values: List<NeoListItem>) {
        writeItems(KEY_NOTE_ITEMS, values)
    }

    fun taskItems(): List<NeoListItem> = readItems(KEY_TASK_ITEMS, KEY_TASK_TEXT)

    fun setTaskItems(values: List<NeoListItem>) {
        writeItems(KEY_TASK_ITEMS, values)
    }

    fun motionSmoothness(): MotionSmoothness =
        runCatching {
            MotionSmoothness.valueOf(
                prefs.getString(KEY_MOTION_SMOOTHNESS, MotionSmoothness.BALANCED.name)
                    ?: MotionSmoothness.BALANCED.name
            )
        }.getOrDefault(MotionSmoothness.BALANCED)

    fun setMotionSmoothness(value: MotionSmoothness) {
        prefs.edit().putString(KEY_MOTION_SMOOTHNESS, value.name).apply()
    }

    fun reduceMotion(): Boolean = prefs.getBoolean(KEY_REDUCE_MOTION, false)

    fun setReduceMotion(value: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, value).apply()
    }

    fun hideStatusBar(): Boolean = prefs.getBoolean(KEY_HIDE_STATUS_BAR, false)

    fun setHideStatusBar(value: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_STATUS_BAR, value).apply()
    }


    fun customQuotes(): List<String> {
        val raw = prefs.getString(KEY_CUSTOM_QUOTES, null)
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val quote = array.optString(index).trim()
                    if (quote.isNotEmpty() && quote.length <= MAX_QUOTE_LENGTH) add(quote)
                }
            }.distinct().take(MAX_CUSTOM_QUOTES)
        }.getOrDefault(emptyList())
    }

    fun setCustomQuotes(values: List<String>) {
        val array = JSONArray()
        values.map { it.trim().take(MAX_QUOTE_LENGTH) }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX_CUSTOM_QUOTES)
            .forEach(array::put)
        prefs.edit().putString(KEY_CUSTOM_QUOTES, array.toString()).apply()
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
            .put("schemaVersion", 1)
            .put("theme", theme().name)
            .put("use24Hour", use24Hour())
            .put("showAmPm", showAmPm())
                        .put("homeAppCount", homeAppCount())
                                    .put("showWeather", showWeather())
            .put("chatNotificationPackages", JSONArray(chatNotificationPackages()))
            .put("favorites", JSONArray(favorites().toList()))
            .put("tilePositions", JSONObject().apply {
                tilePositions().forEach { (id, position) ->
                    put(id, JSONObject().put("column", position.column).put("row", position.row))
                }
            })
            .put("tileSizes", JSONObject().apply {
                tileSizes().forEach { (id, tileSize) -> put(id, tileSize.name) }
            })
            .put("appTileContentMode", appTileContentMode().name)
            .put("typographyStyle", typographyStyle().name)
            .put("wallpaperUri", wallpaperUri())
            .put("homeAppOrder", JSONArray(homeAppOrder()))
            .put("excludedHomeApps", JSONArray(excludedHomeApps().toList()))
            .put("appShortcut", appShortcutKey())
            .put("noteItems", itemsToJson(noteItems()))
            .put("taskItems", itemsToJson(taskItems()))
            .put("iconPackPackage", iconPackPackage())
            .put("motionSmoothness", motionSmoothness().name)
            .put("reduceMotion", reduceMotion())
            .put("customQuotes", JSONArray(customQuotes()))
        return root.toString()
    }

    fun importBackupJson(raw: String): Boolean {
        if (raw.length > 512 * 1024) return false
        return runCatching {
            val root = JSONObject(raw)
            require(root.optInt("schemaVersion", -1) == 1)
            val theme = runCatching { ThemePreference.valueOf(root.optString("theme")) }.getOrDefault(ThemePreference.SYSTEM)
            val contentMode = runCatching { TileContentMode.valueOf(root.optString("appTileContentMode")) }.getOrDefault(TileContentMode.ICON_TEXT)
            val typography = when (root.optString("typographyStyle")) {
                TypographyStyle.CONDENSED.name -> TypographyStyle.CONDENSED
                else -> TypographyStyle.DEFAULT
            }
            val count = normalizePinnedCount(root.optInt("homeAppCount", 5))
            val motionSmoothness = runCatching {
                MotionSmoothness.valueOf(
                    root.optString("motionSmoothness", MotionSmoothness.BALANCED.name)
                )
            }.getOrDefault(MotionSmoothness.BALANCED)
            val reduceMotion = root.optBoolean("reduceMotion", false)

            fun safeArray(name: String, max: Int): JSONArray {
                val array = root.optJSONArray(name) ?: JSONArray()
                require(array.length() <= max)
                return array
            }

            val favoritesArray = safeArray("favorites", MAX_BACKUP_ITEMS)
            val restoredFavorites = buildSet {
                for (i in 0 until favoritesArray.length()) {
                    val value = favoritesArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }
            val chatArray = safeArray("chatNotificationPackages", 1)
            val restoredChat = buildList {
                for (i in 0 until chatArray.length()) {
                    val value = chatArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }.distinct().take(1)
            val orderArray = safeArray("homeAppOrder", MAX_BACKUP_ITEMS)
            val restoredOrder = buildList {
                for (i in 0 until orderArray.length()) {
                    val value = orderArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }.distinct()
            val excludedArray = safeArray("excludedHomeApps", MAX_BACKUP_ITEMS)
            val restoredExcluded = buildSet {
                for (i in 0 until excludedArray.length()) {
                    val value = excludedArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }

            val quotesArray = safeArray("customQuotes", MAX_BACKUP_ITEMS)
            val restoredQuotes = buildList {
                for (i in 0 until quotesArray.length()) {
                    val value = quotesArray.optString(i).trim()
                    if (value.length in 1..MAX_QUOTE_LENGTH) add(value)
                }
            }.distinct().take(MAX_CUSTOM_QUOTES)

            val positionsObject = root.optJSONObject("tilePositions") ?: JSONObject()
            val restoredPositions = mutableSetOf<String>()
            val positionKeys = positionsObject.keys().asSequence().toList()
            require(positionKeys.size <= MAX_BACKUP_ITEMS)
            positionKeys.forEach { id ->
                val item = positionsObject.optJSONObject(id) ?: return@forEach
                val column = item.optInt("column", -1)
                val row = item.optInt("row", -1)
                if (id.length in 1..128 && column in 0..99 && row in 0..999) {
                    restoredPositions.add(id + "|" + column + "|" + row)
                }
            }

            val sizesObject = root.optJSONObject("tileSizes") ?: JSONObject()
            val restoredSizes = mutableSetOf<String>()
            val sizeKeys = sizesObject.keys().asSequence().toList()
            require(sizeKeys.size <= MAX_BACKUP_ITEMS)
            sizeKeys.forEach { id ->
                val tileSize = runCatching { NeoTileSize.valueOf(sizesObject.optString(id)) }.getOrNull()
                if (id.length in 1..128 && tileSize != null) restoredSizes.add(id + "|" + tileSize.name)
            }

            val wallpaper = root.optString("wallpaperUri", "").takeIf { it.startsWith("content://") && it.length <= 2048 }
            val shortcut = root.optString("appShortcut", "").takeIf { it.length in 1..256 }
            val iconPack = root.optString("iconPackPackage", "").takeIf {
                it.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))
            }

            val editor = prefs.edit()
                .putString(KEY_THEME, theme.name)
                .putBoolean(KEY_24_HOUR, root.optBoolean("use24Hour", true))
                .putBoolean(KEY_SHOW_AM_PM, root.optBoolean("showAmPm", true))
                                .putInt(KEY_HOME_APP_COUNT, count)
                                                .putBoolean(KEY_WEATHER, root.optBoolean("showWeather", false))
                .putStringSet(KEY_CHAT_NOTIFICATION_PACKAGES, restoredChat.toSet())
                .putStringSet(KEY_FAVORITES, restoredFavorites)
                .putStringSet(KEY_TILE_POSITIONS, restoredPositions)
                .putStringSet(KEY_TILE_SIZES, restoredSizes)
                .putString(KEY_APP_TILE_CONTENT_MODE, contentMode.name)
                .putString(KEY_CLOCK_STYLE, typography.name)
                .putString(KEY_MOTION_SMOOTHNESS, motionSmoothness.name)
                .putBoolean(KEY_REDUCE_MOTION, reduceMotion)
                .putBoolean(KEY_HIDE_STATUS_BAR, root.optBoolean("hideStatusBar", false))
                .putString(KEY_HOME_APP_ORDER, JSONArray(restoredOrder).toString())
                .putStringSet(KEY_EXCLUDED_HOME_APPS, restoredExcluded)
            if (wallpaper == null) editor.remove(KEY_WALLPAPER_URI) else editor.putString(KEY_WALLPAPER_URI, wallpaper)
            if (shortcut == null) editor.remove(KEY_APP_SHORTCUT) else editor.putString(KEY_APP_SHORTCUT, shortcut)
            if (iconPack == null) editor.remove(KEY_ICON_PACK_PACKAGE) else editor.putString(KEY_ICON_PACK_PACKAGE, iconPack)
            editor.putString(KEY_NOTE_ITEMS, itemsToJson(jsonToItems(root.optJSONArray("noteItems"))).toString())
            editor.putString(KEY_TASK_ITEMS, itemsToJson(jsonToItems(root.optJSONArray("taskItems"))).toString())
            editor.putString(KEY_CUSTOM_QUOTES, JSONArray(restoredQuotes).toString())
            editor.putBoolean(KEY_HOME_APPS_INITIALIZED, true)
            editor.commit()
        }.getOrDefault(false)
    }

    fun resetCustomizations() {
        prefs.edit().apply {
            remove(KEY_THEME)
            remove("show_date")
            remove("show_tagline")
            remove("show_app_count")
            remove(KEY_24_HOUR)
            remove(KEY_SHOW_AM_PM)
            remove(KEY_HOME_APP_COUNT)
            remove(KEY_WEATHER)
            remove(KEY_CHAT_NOTIFICATION_PACKAGES)
            remove(KEY_FAVORITES)
            remove(KEY_TILE_POSITIONS)
            remove(KEY_TILE_SIZES)
            remove(KEY_APP_TILE_CONTENT_MODE)
            remove(KEY_CLOCK_STYLE)
            remove(KEY_WALLPAPER_URI)
            remove(KEY_NOTE_TEXT)
            remove(KEY_NOTE_ITEMS)
            remove(KEY_TASK_ITEMS)
            remove(KEY_HOME_APP_ORDER)
            remove(KEY_EXCLUDED_HOME_APPS)
            remove(KEY_APP_SHORTCUT)
            remove(KEY_ICON_PACK_PACKAGE)
            remove(KEY_CUSTOM_QUOTES)
            remove(KEY_MOTION_SMOOTHNESS)
            remove(KEY_REDUCE_MOTION)
            remove(KEY_HIDE_STATUS_BAR)
            remove(KEY_HOME_APPS_INITIALIZED)
            apply()
        }
    }

    private fun itemsToJson(items: List<NeoListItem>): JSONArray =
        JSONArray().apply {
            items.take(MAX_BACKUP_ITEMS).forEach {
                put(JSONObject().put("text", it.text.take(1000)).put("checked", it.checked))
            }
        }

    private fun jsonToItems(array: JSONArray?): List<NeoListItem> {
        if (array == null || array.length() > MAX_BACKUP_ITEMS) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val value = item.optString("text").trim()
                if (value.isNotEmpty() && value.length <= 1000) {
                    add(NeoListItem(value, item.optBoolean("checked", false)))
                }
            }
        }
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

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_24_HOUR = "use_24_hour"
        private const val KEY_SHOW_AM_PM = "show_am_pm"
        private const val KEY_HOME_APP_COUNT = "home_app_count"
        private const val KEY_WEATHER = "show_weather"
        private const val KEY_CHAT_NOTIFICATION_PACKAGES = "chat_notification_packages"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_TILE_POSITIONS = "tile_positions"
        private const val KEY_TILE_SIZES = "tile_sizes"
        private const val KEY_APP_TILE_CONTENT_MODE = "app_tile_content_mode"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_NOTE_TEXT = "note_text"
        private const val KEY_NOTE_ITEMS = "note_items"
        private const val KEY_TASK_ITEMS = "task_items"
        private const val KEY_HOME_APP_ORDER = "home_app_order"
        private const val KEY_EXCLUDED_HOME_APPS = "excluded_home_apps"
        private const val KEY_APP_SHORTCUT = "app_shortcut"
        private const val KEY_TASK_TEXT = "task_text"
        private const val KEY_ICON_PACK_PACKAGE = "icon_pack_package"
        private const val KEY_CUSTOM_QUOTES = "custom_quotes"
        private const val KEY_MOTION_SMOOTHNESS = "motion_smoothness"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_HIDE_STATUS_BAR = "hide_status_bar"
        private const val KEY_HOME_APPS_INITIALIZED = "home_apps_initialized"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_LAST_MUSIC_PACKAGE = "last_music_package"
        private const val MAX_BACKUP_ITEMS = 500
        private const val MAX_CUSTOM_QUOTES = 5
        private const val MAX_QUOTE_LENGTH = 300
    }
}
