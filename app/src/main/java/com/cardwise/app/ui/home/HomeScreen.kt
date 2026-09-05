package com.cardwise.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Good to see you",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Pay smarter with CardWise.",
            style = MaterialTheme.typography.headlineMedium,
        )
        RecommendationCard()
    }
}

@Composable
private fun RecommendationCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Smart recommendation",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "Add your first card to unlock personalized payment recommendations.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
