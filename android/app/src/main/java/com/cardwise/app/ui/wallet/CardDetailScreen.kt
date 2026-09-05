package com.cardwise.app.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cardwise.app.domain.model.Card

@Composable
fun CardDetailScreen(
    card: Card,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Card details")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(card.name)
                Text(card.issuer)
                Text("•••• ${card.lastFour}")
                Text(card.network.name)
                if (card.benefits.isEmpty()) {
                    Text("No benefits added")
                } else {
                    Text("Benefits")
                    card.benefits.forEach { benefit ->
                        Text("${benefit.title}: ${benefit.description}")
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Button(onClick = onEdit) { Text("Edit") }
            Button(onClick = onDelete) { Text("Delete") }
        }
    }
}
