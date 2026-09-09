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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.catalog.CardProduct
import com.cardwise.app.domain.model.Card
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.PhysicalCard
import com.cardwise.app.ui.theme.SectionTitle
import com.cardwise.app.ui.wallet.CardWalletViewModel
import com.cardwise.app.ui.wallet.WalletUiState

@Composable
fun CardCatalogScreen(
    viewModel: CardCatalogViewModel,
    walletViewModel: CardWalletViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
    val addedNames = (walletState as? WalletUiState.Success)?.cards?.map { it.name }?.toSet().orEmpty()

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SectionTitle("Card Catalogue", "Discover & add")
                    Text(
                        "Build a deck tailored to the way you pay.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = when (state) {
                    is CardCatalogUiState.Content -> (state as CardCatalogUiState.Content).query
                    is CardCatalogUiState.Empty -> (state as CardCatalogUiState.Empty).query
                    else -> ""
                },
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().testTag("catalog_search"),
                singleLine = true,
                leadingIcon = { Text("⌕") },
                placeholder = { Text("Search HDFC, ICICI, Scapia…") },
                shape = RoundedCornerShape(16.dp)
            )
        }
        item {
            val selected = (state as? CardCatalogUiState.Content)?.filter
                ?: (state as? CardCatalogUiState.Empty)?.filter
                ?: CatalogFilter.ALL
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("catalog_filters"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(CatalogFilter.entries.toList()) { filter ->
                    FilterChip(
                        selected = selected == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label()) }
                    )
                }
            }
        }
        when (val current = state) {
            CardCatalogUiState.Loading -> item {
                Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Loading card catalogue…", Modifier.padding(top = 12.dp))
                }
            }
            is CardCatalogUiState.Unavailable -> item {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Catalogue unavailable", fontWeight = FontWeight.Bold)
                        Text(current.message, color = MaterialTheme.colorScheme.onErrorContainer)
                        Button(onClick = viewModel::refresh) { Text("Try again") }
                    }
                }
            }
            is CardCatalogUiState.Empty -> item {
                Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("No cards found", fontWeight = FontWeight.Bold)
                        Text("Try another search or category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is CardCatalogUiState.Content -> {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${current.visibleProducts.size} products", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (current.query.isNotBlank()) Text("Search results", style = MaterialTheme.typography.labelMedium, color = CardWisePalette.Emerald)
                    }
                }
                if (current.refreshError != null) item {
                    Text("Showing cached catalogue. ${current.refreshError}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(current.visibleProducts, key = { it.productId }) { product ->
                    CatalogProductCard(product = product, added = product.name in addedNames, onAdd = {
                        walletViewModel.addCard(product.toEnrolledCard())
                    })
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
                Text(
                    "Card terms and benefits are shown with source confidence. The current built-in catalogue is a local seed and is not a claim of live issuer verification.",
                    Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CatalogProductCard(product: CardProduct, added: Boolean, onAdd: () -> Unit) {
    AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 5 }) {
        GlassCard(elevated = true, modifier = Modifier.testTag("catalog_card_${product.productId}")) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PhysicalCard(Card(0L, product.issuer, product.name, "----", product.network, benefits = product.benefits), compact = true)
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(product.issuer, style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                        Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        product.rewardProgram?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        product.annualFee?.let { Text("Annual fee: ${it.currency} ${"%.2f".format(it.amount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        product.benefits.firstOrNull()?.let { Text(it.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    if (added) AssistChip(onClick = {}, label = { Text("Added") })
                    else Button(onClick = onAdd, modifier = Modifier.testTag("catalog_add_${product.productId}")) { Text("+ Add") }
                }
            }
        }
    }
}

private fun CatalogFilter.label(): String = when (this) {
    CatalogFilter.ALL -> "All"
    CatalogFilter.CREDIT -> "Credit"
    CatalogFilter.TRAVEL -> "Travel"
    CatalogFilter.SHOPPING -> "Shopping"
    CatalogFilter.UPI -> "UPI"
}

private fun CardProduct.toEnrolledCard(): Card = Card(
    id = 0L,
    issuer = issuer,
    name = name,
    lastFour = "----",
    network = network,
    benefits = benefits
)
