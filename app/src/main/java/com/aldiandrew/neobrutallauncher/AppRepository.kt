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

                AppInfo(
                    label = label,
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name,
                    icon = launcherActivity.getIcon(0)
                )
            }
            .distinctBy { it.packageName + "/" + it.activityName }
            .sortedBy { it.label.lowercase() }
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
