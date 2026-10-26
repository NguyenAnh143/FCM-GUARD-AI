package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoDarkSurfaceVariant,
    onPrimaryContainer = MonoDarkText,
    secondary = MonoDarkText,
    onSecondary = MonoBlack,
    secondaryContainer = MonoDarkSurfaceVariant,
    onSecondaryContainer = MonoDarkText,
    tertiary = MonoDarkSubtext,
    background = MonoDarkBackground,
    onBackground = MonoDarkText,
    surface = MonoDarkSurface,
    onSurface = MonoDarkText,
    surfaceVariant = MonoDarkSurfaceVariant,
    onSurfaceVariant = MonoDarkSubtext,
    outline = MonoDarkBorder,
    error = StatusRed,
    onError = MonoWhite
)

private val LightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoLightSurfaceVariant,
    onPrimaryContainer = MonoLightText,
    secondary = MonoLightText,
    onSecondary = MonoWhite,
    secondaryContainer = MonoLightSurfaceVariant,
    onSecondaryContainer = MonoLightText,
    tertiary = MonoLightSubtext,
    background = MonoLightBackground,
    onBackground = MonoLightText,
    surface = MonoLightSurface,
    onSurface = MonoLightText,
    surfaceVariant = MonoLightSurfaceVariant,
    onSurfaceVariant = MonoLightSubtext,
    outline = MonoLightBorder,
    error = StatusRed,
    onError = MonoWhite
)

@Composable
fun FcmGuardTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
