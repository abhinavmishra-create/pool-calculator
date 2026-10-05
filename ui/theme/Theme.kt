package com.example.poolcalculator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Light = lightColorScheme(
    primary = Color(0xFF006876),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA2EEFF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF4A6267),
    secondaryContainer = Color(0xFFCDE7EC),
    background = Color(0xFFF5FBFC),
    surface = Color(0xFFF5FBFC),
    surfaceVariant = Color(0xFFDBE4E6),
    onSurfaceVariant = Color(0xFF3F484A),
    outlineVariant = Color(0xFFBFC8CA)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF52D7F0),
    onPrimary = Color(0xFF00363E),
    primaryContainer = Color(0xFF004E59),
    onPrimaryContainer = Color(0xFFA2EEFF),
    secondary = Color(0xFFB1CBD0),
    background = Color(0xFF191C1D),
    surface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFF3F484A),
    onSurfaceVariant = Color(0xFFBFC8CA),
    outlineVariant = Color(0xFF3F484A)
)

@Composable
fun PoolCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) Dark else Light
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(colorScheme = colors, content = content)
}
