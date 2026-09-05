package com.cardwise.app.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.model.Card as PaymentCard

@Composable
fun CardWalletScreen(
    viewModel: CardWalletViewModel,
    onAddCard: () -> Unit,
    onOpenCard: (PaymentCard) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Your cards", style = MaterialTheme.typography.headlineMedium)
                Text("Keep your payment options ready", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onAddCard) { Text("Add") }
        }

        when (val current = state) {
            WalletUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is WalletUiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is WalletUiState.Success -> {
                if (current.cards.isEmpty()) EmptyWallet(onAddCard) else LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(current.cards, key = PaymentCard::id) { card ->
                        CardSummary(card, onOpen = { onOpenCard(card) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWallet(onAddCard: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No cards yet", style = MaterialTheme.typography.titleLarge)
        Text("Add your first card to start getting payment recommendations.")
        Button(onClick = onAddCard, modifier = Modifier.padding(top = 12.dp)) {
            Text("Add your first card")
        }
    }
}

@Composable
private fun CardSummary(card: PaymentCard, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "${card.name} ending ${card.lastFour}"
        }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(card.name, style = MaterialTheme.typography.titleMedium)
            Text("${card.issuer} •••• ${card.lastFour}")
            Text(card.network.name, style = MaterialTheme.typography.labelMedium)
            if (card.benefits.isNotEmpty()) {
                Text("${card.benefits.size} benefit${if (card.benefits.size == 1) "" else "s"}")
            }
            Button(onClick = onOpen) { Text("View details") }
        }
    }
}
