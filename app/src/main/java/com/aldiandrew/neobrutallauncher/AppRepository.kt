package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process

class AppRepository(private val context: Context) {

    private val launcherApps: LauncherApps =
        context.getSystemService(LauncherApps::class.java)

    fun loadApps(): List<AppInfo> {
        val user = Process.myUserHandle()

        return launcherApps
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
                    icon = IconPackManager(context).iconFor(
                        activityInfo.packageName,
                        activityInfo.name,
                        baseIcon
                    )
                )
            }
            .distinctBy { it.packageName + "/" + it.activityName }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(app: AppInfo) {
        val key = app.packageName + "/" + app.activityName
        LauncherPreferences(context).recordAppLaunch(key)
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
