package com.cardwise.app.ui.catalog

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.PhysicalCard
import com.cardwise.app.ui.theme.SectionTitle
import com.cardwise.app.ui.wallet.CardWalletViewModel
import com.cardwise.app.ui.wallet.WalletUiState

@Composable
fun CardCatalogScreen(viewModel: CardWalletViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val walletState by viewModel.uiState.collectAsStateWithLifecycle()
    val addedIds = (walletState as? WalletUiState.Success)?.cards?.map { it.name }?.toSet().orEmpty()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    val filters = listOf("All", "Credit", "Travel", "Shopping", "UPI")
    val visible = CardWiseCatalogue.filter { card ->
        val matchesQuery = query.isBlank() || card.name.contains(query, true) || card.issuer.contains(query, true)
        val category = card.benefits.firstOrNull()?.category.orEmpty()
        val matchesFilter = selectedFilter == "All" || selectedFilter == "Credit" ||
            (selectedFilter == "Travel" && category == "travel") ||
            (selectedFilter == "Shopping" && category == "shopping") ||
            (selectedFilter == "UPI" && category == "upi")
        matchesQuery && matchesFilter
    }

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }; SectionTitle("Card Catalogue", "Discover & add") } }
        item { OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Text("⌕") }, placeholder = { Text("Search HDFC, ICICI, Scapia…") }, shape = RoundedCornerShape(16.dp)) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { filters.forEach { filter -> FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) }) } } }
        item { Text("${visible.size} products", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(visible, key = { it.id }) { card ->
            AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 5 }) {
                GlassCard(elevated = true) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PhysicalCard(card.copy(lastFour = "----"), compact = true)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(card.issuer, style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                                Text(card.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                card.benefits.firstOrNull()?.let { Text(it.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            if (card.name in addedIds) AssistChip(onClick = {}, label = { Text("Added") })
                            else Button(onClick = { viewModel.addCard(card.copy(lastFour = "----")) }) { Text("+ Add") }
                        }
                    }
                }
            }
        }
        item { Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) { Text("Catalogue entries are curated demo data in this build. Production card terms and benefits will be backed by verified sources.", Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}
