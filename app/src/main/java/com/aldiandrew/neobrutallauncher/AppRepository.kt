package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import org.json.JSONArray
import org.json.JSONObject
import android.os.Process
import android.os.SystemClock

class AppRepository(private val context: Context) {

    private companion object {
        const val CACHE_TTL_MILLIS = 15_000L
        const val SHORTCUT_CACHE_TTL_MILLIS = 12 * 60 * 60 * 1000L
        const val KEY_SHORTCUT_LABELS = "labels_json"
        const val KEY_SHORTCUT_REFRESHED_AT = "refreshed_at"
        const val KEY_SHORTCUT_PACKAGE_SIGNATURE = "package_signature"
    }

    private val launcherApps: LauncherApps =
        context.getSystemService(LauncherApps::class.java)
    private val iconPackManager = IconPackManager(context)
    private val preferences = LauncherPreferences(context)
    private val shortcutCache = context.getSharedPreferences("neo_brutal_shortcut_cache", Context.MODE_PRIVATE)
    private val cacheLock = Any()
    private var cachedApps: List<AppInfo>? = null
    private var cachedIconPackPackage: String? = null
    private var cachedAtElapsedRealtime = 0L

    fun loadApps(): List<AppInfo> {
        val iconPackPackage = preferences.iconPackPackage()
        val now = SystemClock.elapsedRealtime()

        synchronized(cacheLock) {
            val cached = cachedApps
            if (
                cached != null &&
                cachedIconPackPackage == iconPackPackage &&
                now - cachedAtElapsedRealtime < CACHE_TTL_MILLIS
            ) {
                return cached
            }

            val user = Process.myUserHandle()
            val cachedShortcutLabels = readShortcutLabels()

            val loaded = launcherApps
            .getActivityList(null, user)
            .mapNotNull { launcherActivity ->
                val activityInfo = launcherActivity.activityInfo
                val label = launcherActivity.label?.toString()?.trim().orEmpty()

                if (label.isEmpty()) return@mapNotNull null

                val baseIcon = launcherActivity.getIcon(0)
                AppInfo(
                    label = label,
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name,
                    icon = iconPackManager.iconFor(
                        activityInfo.packageName,
                        activityInfo.name,
                        baseIcon,
                        iconPackPackage
                    ),
                    contextLabels = cachedShortcutLabels[activityInfo.packageName].orEmpty()
                )
            }
            .distinctBy { it.packageName + "/" + it.activityName }
            .sortedBy { it.label.lowercase() }

            cachedApps = loaded
            cachedIconPackPackage = iconPackPackage
            cachedAtElapsedRealtime = SystemClock.elapsedRealtime()
            return loaded
        }
    }

    /**
     * Reads real labels published by each app's Android App Shortcuts.
     * Call from a worker thread: querying shortcut data may be slow.
     * The labels are persisted and refreshed at most every 12 hours, or when
     * the installed launcher-package set changes. This requires this app to be
     * the active/default launcher; otherwise the API intentionally returns no access.
     */
    fun refreshShortcutLabelsIfNeeded(apps: List<AppInfo>): Boolean {
        if (!runCatching { launcherApps.hasShortcutHostPermission() }.getOrDefault(false)) {
            return false
        }

        val packages = apps.map { it.packageName }
            .filter { it != context.packageName }
            .distinct()
            .sorted()
        val packageSignature = packages.joinToString("|").hashCode().toString()
        val now = SystemClock.elapsedRealtime()
        val lastRefresh = shortcutCache.getLong(KEY_SHORTCUT_REFRESHED_AT, 0L)
        val oldSignature = shortcutCache.getString(KEY_SHORTCUT_PACKAGE_SIGNATURE, null)
        if (
            oldSignature == packageSignature &&
            lastRefresh > 0L &&
            now - lastRefresh < SHORTCUT_CACHE_TTL_MILLIS
        ) {
            return false
        }

        val labelsByPackage = mutableMapOf<String, List<String>>()
        for (packageName in packages) {
            val query = ShortcutQuery()
                .setPackage(packageName)
                .setQueryFlags(
                    ShortcutQuery.FLAG_MATCH_MANIFEST or
                        ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        ShortcutQuery.FLAG_MATCH_PINNED
                )
            val labels = runCatching {
                launcherApps.getShortcuts(query, Process.myUserHandle())
                    .orEmpty()
                    .asSequence()
                    .filter { it.isEnabled }
                    .mapNotNull { it.shortLabel?.toString()?.trim()?.takeIf(String::isNotEmpty) }
                    .distinct()
                    .take(2)
                    .toList()
            }.getOrDefault(emptyList())
            if (labels.isNotEmpty()) labelsByPackage[packageName] = labels
        }

        val json = JSONObject()
        labelsByPackage.forEach { (packageName, labels) ->
            json.put(packageName, JSONArray(labels))
        }
        shortcutCache.edit()
            .putString(KEY_SHORTCUT_LABELS, json.toString())
            .putString(KEY_SHORTCUT_PACKAGE_SIGNATURE, packageSignature)
            .putLong(KEY_SHORTCUT_REFRESHED_AT, now)
            .apply()

        synchronized(cacheLock) {
            cachedApps = null
            cachedIconPackPackage = null
            cachedAtElapsedRealtime = 0L
        }
        return true
    }

    private fun readShortcutLabels(): Map<String, List<String>> {
        val raw = shortcutCache.getString(KEY_SHORTCUT_LABELS, null) ?: return emptyMap()
        return runCatching {
            val json = JSONObject(raw)
            val labels = mutableMapOf<String, List<String>>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val packageName = keys.next()
                val array = json.optJSONArray(packageName) ?: continue
                labels[packageName] = (0 until array.length())
                    .mapNotNull { index -> array.optString(index).trim().takeIf(String::isNotEmpty) }
                    .take(2)
            }
            labels
        }.getOrDefault(emptyMap())
    }

    fun launch(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
