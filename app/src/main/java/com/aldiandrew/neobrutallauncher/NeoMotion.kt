package com.aldiandrew.neobrutallauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

    fun launchDurationMillis(): Int = when (smoothness) {
        MotionSmoothness.SNAPPY -> 90
        MotionSmoothness.BALANCED -> 120
        MotionSmoothness.FLUID -> 150
    }

    fun returnDurationMillis(): Int = when (smoothness) {
        MotionSmoothness.SNAPPY -> 100
        MotionSmoothness.BALANCED -> 140
        MotionSmoothness.FLUID -> 180
    }
}

val LocalNeoMotionConfig = staticCompositionLocalOf { NeoMotionConfig() }

@Composable
fun NeoHomeReturnMotion(
    trigger: Int,
    config: NeoMotionConfig = LocalNeoMotionConfig.current,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(trigger) {
        if (trigger == 0 || config.reduceMotion) {
            scale.snapTo(1f)
            alpha.snapTo(1f)
            return@LaunchedEffect
        }

        scale.snapTo(0.985f)
        alpha.snapTo(0.96f)

        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = config.returnDurationMillis())
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = (config.returnDurationMillis() - 20).coerceAtLeast(1))
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        },
        content = content
    )
}


fun Dp.neoPressTarget(pressed: Boolean): Dp = if (pressed) this else 0.dp
