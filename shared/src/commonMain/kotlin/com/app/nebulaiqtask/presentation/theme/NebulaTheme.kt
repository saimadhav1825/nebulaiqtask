package com.app.nebulaiqtask.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NebulaColors {
    val DeepBackground = Color(0xFF0F172A)     // Slate 900
    val SurfaceDark = Color(0xFF1E293B)        // Slate 800
    val CardElevated = Color(0xFF26334D)       // Slate 700 elevated
    val CardBorder = Color(0xFF334155)         // Slate 700 border

    val PrimaryIndigo = Color(0xFF6366F1)      // Indigo 500
    val PrimaryIndigoDark = Color(0xFF4F46E5)  // Indigo 600
    val AccentCyan = Color(0xFF06B6D4)         // Cyan 500

    val SafeEmerald = Color(0xFF10B981)        // Emerald 500 (inside fence)
    val SafeEmeraldContainer = Color(0xFF064E3B)
    val WarningAmber = Color(0xFFF59E0B)       // Amber 500
    val CriticalCrimson = Color(0xFFEF4444)    // Crimson 500 (breach exit)
    val CriticalCrimsonContainer = Color(0xFF7F1D1D)

    val TextPrimary = Color(0xFFF8FAFC)        // Slate 50
    val TextSecondary = Color(0xFF94A3B8)      // Slate 400
    val TextTertiary = Color(0xFF64748B)       // Slate 500
}

private val DarkColorScheme = darkColorScheme(
    primary = NebulaColors.PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = NebulaColors.CardElevated,
    onPrimaryContainer = NebulaColors.TextPrimary,
    secondary = NebulaColors.AccentCyan,
    onSecondary = Color.Black,
    background = NebulaColors.DeepBackground,
    onBackground = NebulaColors.TextPrimary,
    surface = NebulaColors.SurfaceDark,
    onSurface = NebulaColors.TextPrimary,
    error = NebulaColors.CriticalCrimson,
    onError = Color.White,
    errorContainer = NebulaColors.CriticalCrimsonContainer,
    onErrorContainer = Color.White
)

@Composable
fun NebulaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
