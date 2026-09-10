package com.cardwise.app.ui.offers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.offers.Offer
import com.cardwise.app.domain.resource.ResourceConfidence
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.SectionTitle

@Composable
fun OffersScreen(
    viewModel: OffersViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filters = listOf("All", "Dining", "Shopping", "Travel")
    val currentFilter = (state as? OffersUiState.Content)?.filter ?: "All"
    val offers = (state as? OffersUiState.Content)?.offers.orEmpty().filter { currentFilter == "All" || it.category.equals(currentFilter, ignoreCase = true) }

    LazyColumn(
        modifier.fillMaxSize().testTag("offers_content"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SectionTitle("Active Card Promos", "Relevant offers with source context") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    FilterChip(selected = currentFilter == filter, onClick = { viewModel.setFilter(filter) }, label = { Text(filter) })
                }
            }
        }
        when (val current = state) {
            OffersUiState.Loading -> item {
                Column(Modifier.fillMaxWidth().padding(vertical = 36.dp)) {
                    CircularProgressIndicator()
                    Text("Loading offers…", Modifier.padding(top = 12.dp))
                }
            }
            is OffersUiState.Unavailable -> item {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Offers unavailable", fontWeight = FontWeight.Bold)
                        Text(current.message, color = MaterialTheme.colorScheme.onErrorContainer)
                        Button(onClick = viewModel::refresh) { Text("Try again") }
                    }
                }
            }
            is OffersUiState.Empty -> item {
                Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
                    Text("No active offers match this category.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is OffersUiState.Content -> {
                item {
                    if (current.refreshError != null) {
                        Text("Showing cached offers. ${current.refreshError}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("${offers.size} visible", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(offers, key = { it.offerId }) { offer -> OfferCard(offer) }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
                Text(
                    "Exact eligibility is re-evaluated with merchant, payment amount and enrolled-card context during recommendation. This bundled source is not live issuer verification.",
                    Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OfferCard(offer: Offer) {
    AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 5 }) {
        GlassCard(elevated = true, modifier = Modifier.testTag("offer_${offer.offerId}")) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(offer.merchantName.uppercase(), style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                    offer.metadata.expiresAt?.let { Text("Ends ${it.toString().take(10)}", style = MaterialTheme.typography.labelSmall) }
                }
                Text(offer.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(offer.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(offer.category ?: "General", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Sky, fontWeight = FontWeight.Bold)
                    Text("${offer.metadata.sourceId} • ${offer.metadata.confidence.label()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                offer.terms?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

private fun ResourceConfidence.label(): String = when (this) {
    ResourceConfidence.VERIFIED -> "Verified"
    ResourceConfidence.HIGH -> "High confidence"
    ResourceConfidence.MEDIUM -> "Medium confidence"
    ResourceConfidence.LOW -> "Low confidence"
    ResourceConfidence.UNKNOWN -> "Unverified"
}
