package com.aldiandrew.neobrutallauncher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class OnboardingStep(val eyebrow: String) {
    WELCOME("01 / WELCOME"),
    ACKNOWLEDGEMENT("02 / PRINCIPLES"),
    PREFERENCES("03 / PREFERENCES"),
    FEATURES("04 / FEATURES"),
    FINALIZE("05 / READY")
}

@Composable
fun NeoOnboardingScreen(
    initialTheme: ThemePreference,
    initialUse24Hour: Boolean,
    initialHideStatusBar: Boolean,
    onThemeChange: (ThemePreference) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onHideStatusBarChange: (Boolean) -> Unit,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var theme by remember { mutableStateOf(initialTheme) }
    var use24Hour by remember { mutableStateOf(initialUse24Hour) }
    var hideStatusBar by remember { mutableStateOf(initialHideStatusBar) }

    fun finish() {
        onFinish()
    }

    val steps = OnboardingStep.entries

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrutalBlock(
                    modifier = Modifier.weight(1f),
                    background = BrutalColors.Cyan,
                    borderWidth = 4.dp,
                    shadowX = 6.dp,
                    shadowY = 6.dp
                ) {
                    Text(
                        text = steps[step].eyebrow,
                        fontFamily = BrutalTypography.Display,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = BrutalColors.Ink
                    )
                }

                if (step > 0) {
                    Spacer(Modifier.width(10.dp))
                    BrutalPressableBlock(
                        modifier = Modifier.width(72.dp),
                        background = BrutalColors.White,
                        borderWidth = 3.dp,
                        shadowX = 3.dp,
                        shadowY = 3.dp,
                        onClick = ::finish
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SKIP",
                                fontFamily = BrutalTypography.Display,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = BrutalColors.Ink
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .background(
                                if (index <= step) BrutalColors.Ink else BrutalColors.White
                            )
                    )
                }
            }

            AnimatedContent(
                targetState = steps[step],
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val forward = targetState.ordinal >= initialState.ordinal
                    (
                        slideInHorizontally(
                            initialOffsetX = { width ->
                                if (forward) width else -width
                            },
                            animationSpec = tween(180)
                        ) + fadeIn(tween(180))
                    ).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { width ->
                                if (forward) -width else width
                            },
                            animationSpec = tween(180)
                        ) + fadeOut(tween(180))
                    ) using SizeTransform(clip = false)
                },
                label = "onboarding-step"
            ) { current ->
                when (current) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.ACKNOWLEDGEMENT -> AcknowledgementStep()
                    OnboardingStep.PREFERENCES -> PreferencesStep(
                        theme = theme,
                        use24Hour = use24Hour,
                        hideStatusBar = hideStatusBar,
                        onThemeChange = {
                            theme = it
                            onThemeChange(it)
                        },
                        onUse24HourChange = {
                            use24Hour = it
                            onUse24HourChange(it)
                        },
                        onHideStatusBarChange = {
                            hideStatusBar = it
                            onHideStatusBarChange(it)
                        }
                    )
                    OnboardingStep.FEATURES -> FeaturesStep()
                    OnboardingStep.FINALIZE -> FinalizeStep()
                }
            }

            BrutalActionButton(
                title = when {
                    step == steps.lastIndex -> "LET ME IN"
                    else -> "CONTINUE"
                },
                background = when {
                    step == steps.lastIndex -> BrutalColors.Yellow
                    else -> BrutalColors.Pink
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (step == steps.lastIndex) {
                    finish()
                } else {
                    step += 1
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = BrutalColors.Yellow,
            borderWidth = 5.dp,
            shadowX = 8.dp,
            shadowY = 8.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "NB",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 64.sp,
                    lineHeight = 60.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    text = "A HOME SCREEN WITH HARD EDGES.",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 23.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    text = "Fast access. Bold blocks. No visual noise pretending to be productivity.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Ink
                )
            }
        }
    }
}

@Composable
private fun AcknowledgementStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = BrutalColors.Pink,
            borderWidth = 5.dp,
            shadowX = 8.dp,
            shadowY = 8.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "NO ROOT REQUIRED",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 30.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    "NB Launcher uses normal Android launcher APIs. Optional features may request Location or Notification Access.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Ink
                )
                BrutalBlock(
                    modifier = Modifier.fillMaxWidth(),
                    background = BrutalColors.White,
                    borderWidth = 3.dp,
                    shadowX = 3.dp,
                    shadowY = 3.dp
                ) {
                    Text(
                        "YOU STAY IN CONTROL. PERMISSIONS ARE OPTIONAL AND FEATURE-SPECIFIC.",
                        fontFamily = BrutalTypography.Display,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = BrutalColors.Ink
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferencesStep(
    theme: ThemePreference,
    use24Hour: Boolean,
    hideStatusBar: Boolean,
    onThemeChange: (ThemePreference) -> Unit,
    onUse24HourChange: (Boolean) -> Unit,
    onHideStatusBarChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "START WITH THE BASICS",
            fontFamily = BrutalTypography.Display,
            fontSize = 27.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))

        PreferenceBlock("APPEARANCE", BrutalColors.Cyan) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    ThemePreference.SYSTEM to "SYSTEM",
                    ThemePreference.LIGHT to "LIGHT",
                    ThemePreference.DARK to "DARK"
                ).forEach { (value, label) ->
                    OnboardingChoiceButton(
                        label = label,
                        selected = theme == value,
                        background = when (value) {
                            ThemePreference.SYSTEM -> BrutalColors.Cyan
                            ThemePreference.LIGHT -> BrutalColors.Yellow
                            ThemePreference.DARK -> BrutalColors.Pink
                        },
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeChange(value) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OnboardingSwitch(
            title = "24-HOUR TIME",
            description = "Use 24-hour time on Home.",
            checked = use24Hour,
            background = BrutalColors.Yellow,
            onCheckedChange = onUse24HourChange
        )

        Spacer(Modifier.height(8.dp))

        OnboardingSwitch(
            title = "HIDE STATUS BAR",
            description = "Use the full launcher canvas and reveal the status bar temporarily with an edge swipe.",
            checked = hideStatusBar,
            background = BrutalColors.Pink,
            onCheckedChange = onHideStatusBarChange
        )
    }
}

@Composable
private fun FeaturesStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "DESIGNED AROUND THREE PAGES",
            fontFamily = BrutalTypography.Display,
            fontSize = 27.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )

        FeatureCard("HOME", "Clock, weather, battery, network, Tasks, pinned apps and shortcuts.", BrutalColors.Yellow)
        FeatureCard("APPS", "Search, alphabetical scrubber, pinning and app actions.", BrutalColors.Cyan)
        FeatureCard("LIVE", "Calendar, chat notifications, current music, progress and quotes.", BrutalColors.Pink)
    }
}

@Composable
private fun FinalizeStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = BrutalColors.Cyan,
            borderWidth = 5.dp,
            shadowX = 8.dp,
            shadowY = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "READY",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 52.sp,
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    "Make NB Launcher your Home app, then build a layout that feels like yours.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Ink
                )
            }
        }
    }
}

@Composable
private fun OnboardingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    background: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    BrutalBlock(
        modifier = Modifier.fillMaxWidth(),
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontFamily = BrutalTypography.Display,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    description,
                    fontSize = 9.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Ink
                )
            }
            Spacer(Modifier.width(8.dp))
            BrutalToggle(
                checked = checked,
                accent = BrutalColors.White,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun PreferenceBlock(
    title: String,
    background: Color,
    content: @Composable () -> Unit
) {
    BrutalBlock(
        modifier = Modifier.fillMaxWidth(),
        background = background,
        borderWidth = 3.dp,
        shadowX = 5.dp,
        shadowY = 5.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                title,
                fontFamily = BrutalTypography.Display,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = BrutalColors.Ink
            )
            content()
        }
    }
}

@Composable
private fun FeatureCard(title: String, body: String, background: Color) {
    BrutalBlock(
        modifier = Modifier.fillMaxWidth(),
        background = background,
        borderWidth = 3.dp,
        shadowX = 4.dp,
        shadowY = 4.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                title,
                fontFamily = BrutalTypography.Display,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = BrutalColors.Ink
            )
            Text(
                body,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrutalColors.Ink
            )
        }
    }
}

@Composable
private fun OnboardingChoiceButton(
    label: String,
    selected: Boolean,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    BrutalBlock(
        modifier = modifier.clickable(onClick = onClick),
        background = if (selected) background else BrutalColors.White,
        borderWidth = 3.dp,
        shadowX = if (selected) 0.dp else 3.dp,
        shadowY = if (selected) 0.dp else 3.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontFamily = BrutalTypography.Display,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = BrutalColors.Ink
            )
        }
    }
}
