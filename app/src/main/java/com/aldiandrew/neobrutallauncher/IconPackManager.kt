package com.aldiandrew.neobrutallauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser

data class IconPackInfo(
    val packageName: String,
    val label: String
)

class IconPackManager(private val context: Context) {
    private val packageManager = context.packageManager
    private val preferences = LauncherPreferences(context)
    private val drawableNameRegex = Regex("[A-Za-z0-9_]+")
    private var cachedPackage: String? = null
    private var cachedMappings: Map<String, String> = emptyMap()
    private var cachedResources: Resources? = null

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
        val packPackage = preferences.iconPackPackage() ?: return fallback
        ensureCache(packPackage)
        val drawableName = cachedMappings["ComponentInfo{$packageName/$activityName}"]
            ?: return fallback

        return runCatching {
            val resources = cachedResources ?: return@runCatching fallback
            val safeName = drawableName.takeIf(drawableNameRegex::matches)
                ?: return@runCatching fallback
            val drawableId = resources.getIdentifier(safeName, "drawable", packPackage)
                .takeIf { it != 0 }
                ?: resources.getIdentifier(safeName, "mipmap", packPackage)
            if (drawableId == 0) fallback
            else resources.getDrawable(drawableId, context.theme)
        }.getOrDefault(fallback)
    }

    private fun ensureCache(packPackage: String) {
        if (cachedPackage == packPackage) return

        val result = runCatching {
            val packInfo = packageManager.getApplicationInfo(packPackage, 0)
            val resources = packageManager.getResourcesForApplication(packInfo)
            val xmlId = resources.getIdentifier("appfilter", "xml", packPackage)
            val mappings = if (xmlId == 0) {
                emptyMap()
            } else {
                val parsed = HashMap<String, String>()
                val parser = resources.getXml(xmlId)
                try {
                    while (parser.next() != XmlPullParser.END_DOCUMENT) {
                        if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                            val component = parser.getAttributeValue(null, "component")
                            val drawable = parser.getAttributeValue(null, "drawable")
                            if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                                parsed[component] = drawable
                            }
                        }
                    }
                } finally {
                    parser.close()
                }
                parsed
            }
            resources to mappings
        }.getOrNull()

        cachedPackage = packPackage
        cachedResources = result?.first
        cachedMappings = result?.second.orEmpty()
    }

}
