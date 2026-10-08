package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.os.SystemClock

class AppRepository(private val context: Context) {

    private companion object {
        const val CACHE_TTL_MILLIS = 15_000L
    }

    private val launcherApps: LauncherApps =
        context.getSystemService(LauncherApps::class.java)
    private val iconPackManager = IconPackManager(context)
    private val preferences = LauncherPreferences(context)
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
                        baseIcon
                    )
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

    fun launch(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
