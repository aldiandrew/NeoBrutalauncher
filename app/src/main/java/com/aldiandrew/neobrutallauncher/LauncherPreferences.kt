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
        return prefs.getInt(KEY_HOME_APP_COUNT, 6).coerceIn(2, 6)
    }

    fun setHomeAppCount(value: Int) {
        prefs.edit().putInt(KEY_HOME_APP_COUNT, value.coerceIn(2, 6)).apply()
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

    fun favorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet().orEmpty()
    }

    fun setFavorites(values: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, values.toSet()).apply()
    }

    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_24_HOUR = "use_24_hour"
        private const val KEY_DATE = "show_date"
        private const val KEY_HOME_APP_COUNT = "home_app_count"
        private const val KEY_TAGLINE = "show_tagline"
        private const val KEY_APP_COUNT = "show_app_count"
        private const val KEY_FAVORITES = "favorites"
    }
}
