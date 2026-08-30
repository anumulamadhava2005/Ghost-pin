package com.ghostpin.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MonochromeDarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoSurfaceVariant,
    onPrimaryContainer = MonoTextPrimary,
    secondary = MonoLightGrey,
    onSecondary = MonoBlack,
    background = MonoBackground,
    onBackground = MonoTextPrimary,
    surface = MonoSurface,
    onSurface = MonoTextPrimary,
    surfaceVariant = MonoSurfaceVariant,
    onSurfaceVariant = MonoTextSecondary,
    outline = MonoBorder,
    outlineVariant = MonoBorderSubtle,
    error = MonoEmergency,
    onError = MonoWhite
)

private val MonochromeLightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = Color(0xFFE4E4E7),
    onPrimaryContainer = MonoBlack,
    secondary = Color(0xFF3F3F46),
    onSecondary = MonoWhite,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF09090B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF09090B),
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF52525B),
    outline = Color(0xFFE4E4E7),
    outlineVariant = Color(0xFFF4F4F5),
    error = MonoEmergency,
    onError = MonoWhite
)

@Composable
fun GhostPinTheme(
    darkTheme: Boolean = true, // Default to sleek monochrome dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MonochromeDarkColorScheme else MonochromeLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
