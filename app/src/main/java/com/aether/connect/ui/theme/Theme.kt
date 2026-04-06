package com.aether.connect.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AetherDarkColorScheme = darkColorScheme(
    primary = AetherCyan,
    onPrimary = AetherDeep,
    primaryContainer = AetherCyanDark,
    onPrimaryContainer = AetherTextPrimary,

    secondary = AetherViolet,
    onSecondary = AetherDeep,
    secondaryContainer = AetherVioletDark,
    onSecondaryContainer = AetherTextPrimary,

    tertiary = AetherAmber,
    onTertiary = AetherDeep,

    background = AetherDeep,
    onBackground = AetherTextPrimary,

    surface = AetherSurface,
    onSurface = AetherTextPrimary,
    surfaceVariant = AetherSurfaceAlt,
    onSurfaceVariant = AetherTextSecondary,

    outline = AetherBorder,
    outlineVariant = AetherBorder,

    error = AetherRed,
    onError = AetherTextPrimary,

    inverseSurface = AetherTextPrimary,
    inverseOnSurface = AetherDeep,
    inversePrimary = AetherCyanDark,

    surfaceTint = AetherCyan,
)

@Composable
fun AetherConnectTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AetherDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AetherDeep.toArgb()
            window.navigationBarColor = AetherDeep.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AetherTypography,
        content = content
    )
}
