package com.servicedesk.app.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueDark = Color(0xFF1D4ED8)
val BackgroundLight = Color(0xFFF3F4F6)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceMuted = Color(0xFF6B7280)

val DarkBackground = Color(0xFF050505)
val DarkSurface = Color(0xFF141414)
val DarkPrimary = Color(0xFF3B82F6)

fun parseHexColor(hex: String, fallback: Color = PrimaryBlue): Color {
    val normalized = hex.removePrefix("#")
    if (normalized.length != 6) return fallback
    return runCatching {
        val rgb = normalized.toLong(16)
        Color(
            red = ((rgb shr 16) and 0xFF) / 255f,
            green = ((rgb shr 8) and 0xFF) / 255f,
            blue = (rgb and 0xFF) / 255f,
        )
    }.getOrDefault(fallback)
}
