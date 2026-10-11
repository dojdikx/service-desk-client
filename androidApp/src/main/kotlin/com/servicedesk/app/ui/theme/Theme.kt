package com.servicedesk.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceLight,
    secondary = PrimaryBlueDark,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurfaceVariant = OnSurfaceMuted,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFFC4C4C4),
)

@Composable
fun ServiceDeskTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
