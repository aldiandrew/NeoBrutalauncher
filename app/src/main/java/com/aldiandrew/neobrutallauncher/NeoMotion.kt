package com.aldiandrew.neobrutallauncher

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf

enum class MotionSmoothness(val label: String) {
    SNAPPY("SNAPPY"),
    BALANCED("BALANCED"),
    FLUID("FLUID")
}

data class NeoMotionConfig(
    val smoothness: MotionSmoothness = MotionSmoothness.BALANCED,
    val reduceMotion: Boolean = false
) {
    fun <T> springSpec(): SpringSpec<T> {
        val (stiffness, dampingRatio) = when (smoothness) {
            MotionSmoothness.SNAPPY -> 650f to 0.92f
            MotionSmoothness.BALANCED -> 500f to 0.88f
            MotionSmoothness.FLUID -> 360f to 0.82f
        }
        return spring(
            stiffness = stiffness,
            dampingRatio = dampingRatio
        )
    }
}

val LocalNeoMotionConfig = staticCompositionLocalOf { NeoMotionConfig() }
