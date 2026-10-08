package com.aldiandrew.neobrutallauncher

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process

class AppRepository(private val context: Context) {

    private val launcherApps: LauncherApps =
        context.getSystemService(LauncherApps::class.java)
    private val iconPackManager = IconPackManager(context)

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
                    icon = iconPackManager.iconFor(
                        activityInfo.packageName,
                        activityInfo.name,
                        baseIcon
                    )
                )
            }
            .distinctBy { it.packageName + "/" + it.activityName }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(app: AppInfo): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return runCatching {
            val options = ActivityOptions.makeCustomAnimation(
                context,
                R.anim.nb_app_enter,
                R.anim.nb_app_exit
            )
            context.startActivity(intent, options.toBundle())
        }.isSuccess
    }
}
