package com.cardwise.app.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardwise.app.domain.model.Card

/** Shared enrolled-card presentation. Card data remains supplied by the wallet/catalogue engines. */
@Composable
fun CardDeckItem(
    card: Card,
    onOpenCard: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        elevated = true,
        modifier = modifier.semantics {
            contentDescription = "${card.name} ending ${card.lastFour}"
        }
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PhysicalCard(card)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            card.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (card.isActive) {
                            Text(
                                "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CardWisePalette.Emerald
                            )
                        }
                    }
                    Text(
                        "${card.issuer} • ${card.network.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (card.benefits.isNotEmpty()) {
                        Text(
                            "${card.benefits.size} benefit${if (card.benefits.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CardWisePalette.Emerald
                        )
                    }
                }
                TextButton(onClick = { onOpenCard(card) }) {
                    Text("Details")
                }
            }
        }
    }
}
