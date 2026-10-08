package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.util.Locale

@Composable
fun NeoOnboardingScreen(
    apps: List<AppInfo>,
    initialFavorites: Set<String>,
    favoriteLimit: Int,
    locationPermissionGranted: Boolean,
    notificationAccessGranted: Boolean,
    onRequestLocationPermission: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onFavoritesChange: (Set<String>) -> Unit,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var favorites by remember(initialFavorites) {
        mutableStateOf(initialFavorites)
    }

    val normalizedLimit = favoriteLimit.coerceIn(1, 7)
    val steps = 4

    fun updateFavorites(app: AppInfo) {
        val key = app.packageName + "/" + app.activityName
        val updated = favorites.toMutableSet()
        if (updated.contains(key)) {
            updated.remove(key)
        } else if (updated.size < normalizedLimit) {
            updated.add(key)
        }
        favorites = updated
        onFavoritesChange(updated)
    }

    fun finish() {
        onFavoritesChange(favorites)
        onFinish()
    }

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
                        text = when (step) {
                            0 -> "01 / WELCOME"
                            1 -> "02 / PERMISSIONS"
                            2 -> "03 / FAVORITES"
                            else -> "04 / READY"
                        },
                        fontFamily = BrutalTypography.Display,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = BrutalColors.Ink
                    )
                }

                if (step > 0) {
                    Spacer(Modifier.width(10.dp))
                    BrutalPressableBlock(
                        modifier = Modifier.size(width = 72.dp, height = 40.dp),
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
                                text = "SKIP",
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
                repeat(steps) { index ->
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

            Box(modifier = Modifier.weight(1f)) {
                when (step) {
                    0 -> WelcomeStep()
                    1 -> PermissionsStep(
                        locationPermissionGranted = locationPermissionGranted,
                        notificationAccessGranted = notificationAccessGranted,
                        onRequestLocationPermission = onRequestLocationPermission,
                        onOpenNotificationAccess = onOpenNotificationAccess
                    )
                    2 -> FavoriteAppsStep(
                        apps = apps,
                        favorites = favorites,
                        favoriteLimit = normalizedLimit,
                        onToggleFavorite = ::updateFavorites
                    )
                    else -> ReadyStep(favoritesCount = favorites.size)
                }
            }

            BrutalActionButton(
                title = if (step == steps - 1) "LET ME IN" else "CONTINUE",
                background = if (step == steps - 1) {
                    BrutalColors.Yellow
                } else {
                    BrutalColors.Pink
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (step == steps - 1) {
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
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp),
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
                    text = "Start by choosing the apps you want to keep closest. You can change your favorites later.",
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
private fun PermissionsStep(
    locationPermissionGranted: Boolean,
    notificationAccessGranted: Boolean,
    onRequestLocationPermission: () -> Unit,
    onOpenNotificationAccess: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PERMISSIONS",
            fontFamily = BrutalTypography.Display,
            fontSize = 28.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "A FEW OPTIONAL PERMISSIONS POWER THE LIVE FEATURES.",
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = if (locationPermissionGranted) BrutalColors.Yellow else BrutalColors.Cyan,
            borderWidth = 3.dp,
            shadowX = 4.dp,
            shadowY = 4.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LOCATION",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    text = if (locationPermissionGranted) {
                        "GRANTED — WEATHER CAN USE YOUR DEVICE LOCATION."
                    } else {
                        "USED ONLY TO SHOW LOCAL WEATHER ON HOME."
                    },
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Ink
                )
                if (!locationPermissionGranted) {
                    BrutalActionButton(
                        title = "ALLOW LOCATION",
                        background = BrutalColors.Yellow,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onRequestLocationPermission
                    )
                }
            }
        }

        BrutalBlock(
            modifier = Modifier.fillMaxWidth(),
            background = if (notificationAccessGranted) BrutalColors.Yellow else BrutalColors.Pink,
            borderWidth = 3.dp,
            shadowX = 4.dp,
            shadowY = 4.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NOTIFICATION ACCESS",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    text = if (notificationAccessGranted) {
                        "GRANTED — MUSIC AND LIVE CHAT CAN READ THEIR NOTIFICATION DATA."
                    } else {
                        "USED BY THE MUSIC AND LIVE CHAT TILES."
                    },
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Ink
                )
                BrutalActionButton(
                    title = if (notificationAccessGranted) "MANAGE ACCESS" else "OPEN NOTIFICATION ACCESS",
                    background = BrutalColors.Yellow,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenNotificationAccess
                )
            }
        }
    }
}

@Composable
private fun FavoriteAppsStep(
    apps: List<AppInfo>,
    favorites: Set<String>,
    favoriteLimit: Int,
    onToggleFavorite: (AppInfo) -> Unit
) {
    val sortedApps = remember(apps) {
        apps.sortedBy { it.label.lowercase(Locale.ENGLISH) }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CHOOSE YOUR FAVORITE APPS",
            fontFamily = BrutalTypography.Display,
            fontSize = 26.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${favorites.size} / $favoriteLimit SELECTED",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (sortedApps.isEmpty()) {
            BrutalBlock(
                modifier = Modifier.fillMaxWidth(),
                background = BrutalColors.Pink,
                borderWidth = 3.dp,
                shadowX = 5.dp,
                shadowY = 5.dp
            ) {
                Text(
                    text = "NO LAUNCHABLE APPS DETECTED YET.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = BrutalColors.Ink
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = sortedApps,
                    key = { it.packageName + "/" + it.activityName }
                ) { app ->
                    val key = app.packageName + "/" + app.activityName
                    val selected = favorites.contains(key)
                    val canSelect = selected || favorites.size < favoriteLimit
                    val icon = remember(app.packageName, app.activityName) {
                        app.icon.toBitmap(64, 64).asImageBitmap()
                    }

                    BrutalBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = canSelect,
                                onClick = { onToggleFavorite(app) }
                            ),
                        background = if (selected) {
                            BrutalColors.Yellow
                        } else {
                            BrutalColors.White
                        },
                        borderWidth = 3.dp,
                        shadowX = if (selected) 0.dp else 3.dp,
                        shadowY = if (selected) 0.dp else 3.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = icon,
                                contentDescription = app.label,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                text = app.label.uppercase(Locale.ENGLISH),
                                modifier = Modifier.weight(1f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (selected) "SELECTED" else "ADD",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalColors.Ink
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadyStep(
    favoritesCount: Int
) {
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
                    text = "READY",
                    fontFamily = BrutalTypography.Display,
                    fontSize = 52.sp,
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrutalColors.Ink
                )
                Text(
                    text = if (favoritesCount > 0) {
                        "$favoritesCount FAVORITE APPS SAVED."
                    } else {
                        "NO FAVORITES SELECTED. HOME WILL USE ITS NORMAL APP LIST."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalColors.Ink
                )
            }
        }
    }
}
