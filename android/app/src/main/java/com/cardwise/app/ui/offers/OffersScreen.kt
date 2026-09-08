package com.cardwise.app.ui.offers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.SectionTitle

data class CardWiseOffer(
    val merchant: String,
    val title: String,
    val detail: String,
    val validity: String,
    val source: String,
    val category: String
)

private val demoOffers = listOf(
    CardWiseOffer("Swiggy", "10% back on eligible spends", "Up to ₹150 • minimum spend ₹1,000", "Ends 30 Sep", "Demo / verify before use", "Dining"),
    CardWiseOffer("Amazon", "Extra reward on eligible purchases", "Applies to enrolled eligible cards", "Ends 15 Oct", "Demo / verify before use", "Shopping"),
    CardWiseOffer("Flights", "Accelerated travel rewards", "Selected travel transactions", "Ongoing", "Card product benefit", "Travel")
)

@Composable
fun OffersScreen(modifier: Modifier = Modifier) {
    var filter by rememberSaveable { mutableStateOf("All") }
    val filters = listOf("All", "Dining", "Shopping", "Travel")
    val offers = demoOffers.filter { filter == "All" || it.category == filter }

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionTitle("Active Offers", "Matched to your deck") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { filters.forEach { FilterChip(selected = filter == it, onClick = { filter = it }, label = { Text(it) }) } } }
        item { Text("${offers.size} visible", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(offers, key = { it.merchant + it.title }) { offer ->
            GlassCard(elevated = true) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(offer.merchant.uppercase(), style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) { Text(offer.validity, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) }
                    }
                    Text(offer.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(offer.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(offer.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(offer.category, style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Sky, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
                Text("Offers in this build are UX fixtures. Live personalized offers require verified issuer/partner integrations and explicit eligibility data.", Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
