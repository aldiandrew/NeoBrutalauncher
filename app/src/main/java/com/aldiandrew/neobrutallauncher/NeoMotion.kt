package com.aldiandrew.neobrutallauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AnimationStyle(val label: String) {
    SMOOTH("SMOOTH"),
    TAP_FLIP("TAP-FLIP"),
    CUBE_3D("3D CUBE")
}

enum class MotionSmoothness(val label: String) {
    SNAPPY("SNAPPY"),
    BALANCED("BALANCED"),
    FLUID("FLUID")
}

data class NeoMotionConfig(
    val animationStyle: AnimationStyle = AnimationStyle.SMOOTH,
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

    fun fadeSpec(): AnimationSpec<Float> =
        if (reduceMotion) {
            tween(durationMillis = 1)
        } else {
            tween(
                durationMillis = when (smoothness) {
                    MotionSmoothness.SNAPPY -> 120
                    MotionSmoothness.BALANCED -> 170
                    MotionSmoothness.FLUID -> 220
                }
            )
        }

    fun launchScaleTarget(): Float = if (reduceMotion) 1f else 14f
}

val LocalNeoMotionConfig = staticCompositionLocalOf { NeoMotionConfig() }

@Composable
fun NeoHomeReturnMotion(
    trigger: Int,
    config: NeoMotionConfig = LocalNeoMotionConfig.current,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (trigger == 0 || config.reduceMotion) {
        Box(modifier = modifier) { content() }
        return
    }

    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(trigger) {
        alpha.snapTo(0f)
        scale.snapTo(0.965f)

        kotlinx.coroutines.coroutineScope {
            launch {
                alpha.animateTo(1f, config.fadeSpec())
            }
            launch {
                scale.animateTo(1f, config.springSpec())
            }
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            scaleX = scale.value
            scaleY = scale.value
        }
    ) {
        content()
    }
}

@Composable
fun NeoLaunchTransition(
    app: AppInfo,
    config: NeoMotionConfig,
    onFinished: () -> Unit
) {
    val iconBitmap = remember(app.packageName, app.activityName, app.icon) {
        app.icon.toBitmap(128, 128).asImageBitmap()
    }
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val rotationY = remember { Animatable(0f) }

    LaunchedEffect(app.packageName, app.activityName, config) {
        if (config.reduceMotion) {
            delay(1)
        } else {
            when (config.animationStyle) {
                AnimationStyle.SMOOTH -> {
                    scale.animateTo(config.launchScaleTarget(), config.springSpec())
                    alpha.animateTo(0f, config.fadeSpec())
                }

                AnimationStyle.TAP_FLIP -> {
                    kotlinx.coroutines.coroutineScope {
                        launch { scale.animateTo(config.launchScaleTarget(), config.springSpec()) }
                        launch { rotationY.animateTo(72f, config.springSpec()) }
                        launch { alpha.animateTo(0f, config.fadeSpec()) }
                    }
                }

                AnimationStyle.CUBE_3D -> {
                    kotlinx.coroutines.coroutineScope {
                        launch { scale.animateTo(config.launchScaleTarget(), config.springSpec()) }
                        launch { rotationY.animateTo(90f, config.springSpec()) }
                        launch { alpha.animateTo(0f, config.fadeSpec()) }
                    }
                }
            }
        }
        onFinished()
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                    this.rotationY = rotationY.value
                    cameraDistance = 18_000f
                }
        ) {
            BrutalBlock(
                modifier = Modifier.fillMaxSize(),
                background = BrutalColors.Cyan,
                borderWidth = 4.dp,
                shadowX = 7.dp,
                shadowY = 7.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier.size(76.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = app.label.uppercase(),
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center,
                        fontFamily = BrutalTypography.Display,
                        fontSize = 15.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalColors.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

fun Dp.neoPressTarget(pressed: Boolean): Dp = if (pressed) this else 0.dp
