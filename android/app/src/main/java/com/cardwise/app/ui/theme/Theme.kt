package com.cardwise.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CardWiseColors = lightColorScheme(
    primary = Color(0xFF2457D6),
    onPrimary = Color.White,
    secondary = Color(0xFF52647A),
    background = Color(0xFFF8F9FC),
    surface = Color.White,
    onSurface = Color(0xFF171A21)
)

private val CardWiseTypography = Typography()

@Composable
fun CardWiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CardWiseColors,
        typography = CardWiseTypography,
        content = content
    )
}
