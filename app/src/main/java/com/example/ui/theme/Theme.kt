package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FlikataPrimary,
    secondary = FlikataSecondary,
    tertiary = Color(0xFF263951),
    background = Color(0xFF18191A),
    surface = Color(0xFF242526),
    surfaceVariant = Color(0xFF3A3B3C),
    onPrimary = Color.White,
    onBackground = Color(0xFFE4E6EB),
    onSurface = Color(0xFFE4E6EB),
    onSurfaceVariant = Color(0xFFB0B3B8),
    outline = Color(0xFF3E4042)
)

private val LightColorScheme = lightColorScheme(
    primary = FlikataPrimary,
    secondary = FlikataSecondary,
    tertiary = FlikataSoftBlue,
    background = FlikataLightBackground,
    surface = FlikataLightSurface,
    surfaceVariant = Color(0xFFF0F2F5),
    onPrimary = Color.White,
    onBackground = FlikataLightTextPrimary,
    onSurface = FlikataLightTextPrimary,
    onSurfaceVariant = FlikataLightTextSecondary,
    outline = FlikataLightDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
