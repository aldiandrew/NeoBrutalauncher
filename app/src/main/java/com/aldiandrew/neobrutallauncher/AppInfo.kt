package com.aldiandrew.neobrutallauncher

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable,
    val contextLabels: List<String> = emptyList()
)
