package com.cardwise.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ObsidianColors = darkColorScheme(
    primary = CardWisePalette.Emerald,
    onPrimary = CardWisePalette.Obsidian,
    secondary = CardWisePalette.Sky,
    background = CardWisePalette.Obsidian,
    surface = CardWisePalette.Ink,
    surfaceContainerLow = Color(0xFF0C1018),
    surfaceContainerHigh = Color(0xFF141C28),
    outlineVariant = CardWisePalette.Line,
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val PearlColors = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    secondary = Color(0xFF0284C7),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B)
)

private val CardWiseTypography = Typography()

@Composable
fun CardWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ObsidianColors else PearlColors,
        typography = CardWiseTypography,
        content = content
    )
}
