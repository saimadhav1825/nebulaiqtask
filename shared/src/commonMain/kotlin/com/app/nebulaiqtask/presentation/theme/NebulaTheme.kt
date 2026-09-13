package com.app.nebulaiqtask.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NebulaColors {
    // Ultra-clean light background and surface system
    val DeepBackground = Color(0xFFF8FAFC)     // Slate 50 ultra-light canvas
    val SurfaceDark = Color(0xFFFFFFFF)        // Pure White card surface
    val CardElevated = Color(0xFFF1F5F9)       // Slate 100 soft neutral container
    val CardBorder = Color(0xFFE2E8F0)         // Slate 200 crisp subtle border

    // Brand Primary & Accents
    val PrimaryIndigo = Color(0xFF2563EB)      // Royal Blue 600
    val PrimaryIndigoDark = Color(0xFF1D4ED8)  // Royal Blue 700
    val PrimaryContainer = Color(0xFFEFF6FF)   // Soft Blue 50 tint
    val AccentCyan = Color(0xFF0284C7)         // Sky 600

    // Status Colors (Safe, Warning, Critical) with Soft Light Containers
    val SafeEmerald = Color(0xFF059669)        // Emerald 600
    val SafeEmeraldContainer = Color(0xFFECFDF5) // Soft Mint 50
    val WarningAmber = Color(0xFFD97706)       // Amber 600
    val WarningAmberContainer = Color(0xFFFFFBEB) // Soft Amber/Cream 50
    val CriticalCrimson = Color(0xFFDC2626)    // Rose 600
    val CriticalCrimsonContainer = Color(0xFFFEF2F2) // Soft Rose 50

    // Owner / Leader Gold Accent
    val GoldOwner = Color(0xFFB45309)          // Warm Gold text/accent
    val GoldOwnerContainer = Color(0xFFFEF3C7) // Soft Gold/Amber pill

    // High-Contrast Legible Light Typography
    val TextPrimary = Color(0xFF0F172A)        // Slate 900
    val TextSecondary = Color(0xFF64748B)      // Slate 500
    val TextTertiary = Color(0xFF94A3B8)       // Slate 400
}

private val LightColorScheme = lightColorScheme(
    primary = NebulaColors.PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = NebulaColors.PrimaryContainer,
    onPrimaryContainer = NebulaColors.PrimaryIndigoDark,
    secondary = NebulaColors.AccentCyan,
    onSecondary = Color.White,
    background = NebulaColors.DeepBackground,
    onBackground = NebulaColors.TextPrimary,
    surface = NebulaColors.SurfaceDark,
    onSurface = NebulaColors.TextPrimary,
    error = NebulaColors.CriticalCrimson,
    onError = Color.White,
    errorContainer = NebulaColors.CriticalCrimsonContainer,
    onErrorContainer = NebulaColors.CriticalCrimson
)

@Composable
fun NebulaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
