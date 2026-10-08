package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser

data class IconPackInfo(
    val packageName: String,
    val label: String
)

class IconPackManager(private val context: Context) {
    private val packageManager = context.packageManager

    fun installedIconPacks(): List<IconPackInfo> {
        val actions = listOf(
            "com.novalauncher.THEME",
            "com.anddoes.launcher.THEME",
            "org.adw.launcher.THEMES",
            "com.teslacoilsw.launcher.THEME"
        )
        return actions
            .flatMap { action ->
                packageManager.queryIntentActivities(Intent(action), PackageManager.MATCH_ALL)
            }
            .mapNotNull { resolveInfo ->
                val app = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                if (app.packageName == context.packageName) return@mapNotNull null
                IconPackInfo(
                    packageName = app.packageName,
                    label = packageManager.getApplicationLabel(app).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun iconFor(
        packageName: String,
        activityName: String,
        fallback: Drawable
    ): Drawable {
        val packPackage = LauncherPreferences(context).iconPackPackage() ?: return fallback
        return runCatching {
            val packInfo = packageManager.getApplicationInfo(packPackage, 0)
            val resources = packageManager.getResourcesForApplication(packInfo)
            val xmlId = resources.getIdentifier("appfilter", "xml", packPackage)
            if (xmlId == 0) return@runCatching fallback

            val targetComponent = "ComponentInfo{$packageName/$activityName}"
            val parser = resources.getXml(xmlId)
            var drawableName: String? = null
            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        if (parser.getAttributeValue(null, "component") == targetComponent) {
                            drawableName = parser.getAttributeValue(null, "drawable")
                            break
                        }
                    }
                }
            } finally {
                parser.close()
            }

            val safeName = drawableName?.takeIf { it.matches(Regex("[A-Za-z0-9_]+")) }
                ?: return@runCatching fallback
            val drawableId = resources.getIdentifier(safeName, "drawable", packPackage)
                .takeIf { it != 0 }
                ?: resources.getIdentifier(safeName, "mipmap", packPackage)
            if (drawableId == 0) fallback
            else resources.getDrawable(drawableId, context.theme)
        }.getOrDefault(fallback)
    }
}
