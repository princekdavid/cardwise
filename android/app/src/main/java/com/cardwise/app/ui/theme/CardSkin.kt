package com.cardwise.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Visual skin selected by trusted catalogue metadata, never by product/issuer name. */
data class CardSkin(
    val gradient: Brush,
    val chip: Color
)

fun cardSkinFor(seed: Int): CardSkin {
    val palettes = listOf(
        listOf(Color(0xFF0F766E), Color(0xFF134E4A)),
        listOf(Color(0xFF111827), Color(0xFF374151)),
        listOf(Color(0xFF6B21A8), Color(0xFF9333EA)),
        listOf(Color(0xFF9A3412), Color(0xFFEA580C)),
        listOf(Color(0xFF0C4A6E), Color(0xFF0369A1)),
        listOf(Color(0xFF172033), Color(0xFF334155))
    )
    val colors = palettes[seed.mod(palettes.size)]
    return CardSkin(Brush.linearGradient(colors), Color(0xFFFDE68A))
}
