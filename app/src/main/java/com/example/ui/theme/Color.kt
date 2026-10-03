package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

val FlikataPrimary = Color(0xFF1877F2)
val FlikataPrimaryDark = Color(0xFF0062E0)
val FlikataSecondary = Color(0xFF4267B2)
val FlikataSoftBlue = Color(0xFFE7F3FF)
val FlikataVerified = Color(0xFF00A884)
val FlikataError = Color(0xFFFA383E)

// Static Light Theme Base Constants
val FlikataLightBackground = Color(0xFFF0F2F5)
val FlikataLightSurface = Color(0xFFFFFFFF)
val FlikataLightTextPrimary = Color(0xFF050505)
val FlikataLightTextSecondary = Color(0xFF65676B)
val FlikataLightDivider = Color(0xFFE4E6EB)

// Dynamic Theme-Adaptive Properties (Automatically adapts to Dark/Light mode in all Composables)
val FlikataBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val FlikataSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val FlikataTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val FlikataTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val FlikataDivider: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
