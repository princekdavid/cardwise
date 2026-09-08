package com.cardwise.app.ui.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.model.Card as PaymentCard
import com.cardwise.app.ui.theme.CardDeckItem
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.PhysicalCard
import com.cardwise.app.ui.theme.SectionTitle

@Composable
fun CardWalletScreen(
    viewModel: CardWalletViewModel,
    onAddCard: () -> Unit,
    onOpenCard: (PaymentCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showActiveOnly by rememberSaveable { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("My Physical Deck", "Your enrolled cards")
            Button(
                onClick = onAddCard,
                modifier = Modifier.semantics { contentDescription = "Add card" }
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Add", modifier = Modifier.padding(start = 6.dp))
            }
        }

        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ready for smarter routing", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text("CardWise uses your enrolled cards to compare rewards.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !showActiveOnly, onClick = { showActiveOnly = false }, label = { Text("All") })
            FilterChip(selected = showActiveOnly, onClick = { showActiveOnly = true }, label = { Text("Active") })
        }

        when (val current = state) {
            WalletUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 40.dp)
            )
            is WalletUiState.Error -> Text(
                current.message,
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.error
            )
            is WalletUiState.Success -> {
                val cards = current.cards.filter { !showActiveOnly || it.isActive }
                if (cards.isEmpty()) EmptyWallet(onAddCard)
                else LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "wallet_spotlight") {
                        CardDeckSpotlight(
                            cards = cards,
                            onOpenCard = onOpenCard
                        )
                    }
                    item(key = "wallet_all_cards") {
                        Text(
                            "ALL ENROLLED CARDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = CardWisePalette.Emerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    items(cards, key = PaymentCard::id) { card ->
                        AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 6 }) {
                            CardDeckItem(
                                card = card,
                                onOpenCard = onOpenCard,
                                modifier = Modifier.testTag("wallet_card_${card.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDeckSpotlight(
    cards: List<PaymentCard>,
    onOpenCard: (PaymentCard) -> Unit
) {
    val spotlight = cards.firstOrNull { it.isActive } ?: cards.first()
    val supporting = cards.filterNot { it.id == spotlight.id }.take(2)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("YOUR SPOTLIGHT", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                Text("Ready to use", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                "${cards.size} card${if (cards.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            Modifier.fillMaxWidth().height(224.dp).testTag("wallet_tactile_deck"),
            contentAlignment = Alignment.TopCenter
        ) {
            supporting.asReversed().forEachIndexed { index, card ->
                PhysicalCard(
                    card = card,
                    compact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .offset(y = (index * 10).dp)
                        .clickable { onOpenCard(card) }
                        .semantics { contentDescription = "${card.name} ending ${card.lastFour}" }
                )
            }
            PhysicalCard(
                card = spotlight,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_spotlight_card_${spotlight.id}")
                    .clickable { onOpenCard(spotlight) }
                    .semantics { contentDescription = "Spotlight ${spotlight.name} ending ${spotlight.lastFour}" }
            )
        }
    }
}

@Composable
private fun EmptyWallet(onAddCard: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(42.dp))
        Text("No cards in your deck", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        Text(
            "Build your deck from the catalogue. CardWise only needs safe card metadata, never PAN, CVV or PIN.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onAddCard, modifier = Modifier.padding(top = 14.dp)) { Text("Browse card catalogue") }
    }
}
