package com.aldiandrew.neobrutallauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
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

        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150)
            )
        }
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 130)
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

@Composable
fun NeoLaunchTransition(
    app: AppInfo?,
    config: NeoMotionConfig,
    onFinished: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val progress = remember(app?.packageName, app?.activityName) { Animatable(0f) }

    LaunchedEffect(app?.packageName, app?.activityName) {
        if (app == null) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }

        if (config.reduceMotion) {
            onFinished()
            return@LaunchedEffect
        }

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 120)
        )
        onFinished()
    }

    Box(
        modifier = Modifier.graphicsLayer {
            val amount = if (app == null) 0f else progress.value
            scaleX = 1f - (amount * 0.025f)
            scaleY = 1f - (amount * 0.025f)
            alpha = 1f - (amount * 0.04f)
        },
        content = content
    )
}

fun Dp.neoPressTarget(pressed: Boolean): Dp = if (pressed) this else 0.dp
