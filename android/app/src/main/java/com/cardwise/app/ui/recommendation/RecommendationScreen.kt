package com.cardwise.app.ui.recommendation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.recommendation.CardRecommendation
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.PhysicalCard
import com.cardwise.app.ui.theme.SectionTitle
import java.util.Locale

@Composable
fun RecommendationScreen(viewModel: RecommendationViewModel, payment: UpiPaymentRequest? = null, onContinueToPayment: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val input = when (state) {
        is RecommendationUiState.Loading -> (state as RecommendationUiState.Loading).input
        is RecommendationUiState.Ready -> (state as RecommendationUiState.Ready).input
        is RecommendationUiState.Empty -> (state as RecommendationUiState.Empty).input
        is RecommendationUiState.Error -> (state as RecommendationUiState.Error).input
    }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionTitle("The best way to pay", "CardWise decision engine", Modifier.semantics { heading() }) }
        payment?.let { item { PaymentSummary(it) } }
        item { InputSection(input, viewModel::setAmount, viewModel::setCategory) }
        when (state) {
            is RecommendationUiState.Loading -> item { DecisionLoader() }
            is RecommendationUiState.Ready -> {
                val ready = state as RecommendationUiState.Ready
                item { Text(if (ready.recommendations.size == 1) "Best match" else "${ready.recommendations.size} ranked matches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(ready.recommendations, key = { it.card.id }, contentType = { "recommendation" }) { RecommendationCard(it) }
                if (payment != null && onContinueToPayment != null) item { Button(onClick = onContinueToPayment, modifier = Modifier.fillMaxWidth()) { Text("Continue to UPI app") } }
            }
            is RecommendationUiState.Empty -> item { EmptyState((state as RecommendationUiState.Empty).input) }
            is RecommendationUiState.Error -> item { ErrorState((state as RecommendationUiState.Error).message, viewModel::retry) }
        }
    }
}

@Composable private fun PaymentSummary(payment: UpiPaymentRequest) {
    GlassCard(elevated = true) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("SCANNED PAYMENT", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
        Text(payment.merchantName ?: "UPI merchant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(payment.vpa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        payment.amount?.let { Text("₹$it", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CardWisePalette.Emerald, modifier = Modifier.padding(top = 8.dp)) }
        payment.note?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    } }
}

@Composable private fun InputSection(input: RecommendationInput, onAmountChange: (String) -> Unit, onCategoryChange: (String) -> Unit) {
    GlassCard { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = input.amount, onValueChange = { value -> if (value.length <= 12 && value.count { it == '.' } <= 1 && value.all { it.isDigit() || it == '.' }) onAmountChange(value) }, modifier = Modifier.fillMaxWidth(), label = { Text("Amount") }, prefix = { Text("₹ ") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        OutlinedTextField(value = input.category, onValueChange = onCategoryChange, modifier = Modifier.fillMaxWidth(), label = { Text("Category") }, placeholder = { Text("Dining, travel, groceries…") }, singleLine = true)
    } }
}

@Composable private fun DecisionLoader() {
    val transition = rememberInfiniteTransition(label = "recommendation_loader")
    val alpha by transition.animateFloat(0.55f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "loader_alpha")
    GlassCard(elevated = true) { Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.graphicsLayer { alpha = alpha }, strokeWidth = 3.dp)
        Column { Text("Routing payment matrix…", fontWeight = FontWeight.Bold); Text("Checking benefits, caps and active rules locally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    } }
}

@Composable private fun RecommendationCard(recommendation: CardRecommendation) {
    val card = recommendation.card
    AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 5 }) {
        GlassCard(elevated = recommendation.rank == 1) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (recommendation.rank == 1) Text("BEST WAY", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
            PhysicalCard(card, compact = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(if (recommendation.rank == 1) "Recommended for this payment" else "Alternative ${recommendation.rank - 1}", fontWeight = FontWeight.Bold); Text("${card.issuer} • ${card.network.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("₹${String.format(Locale.ROOT, "%.2f", recommendation.reward.estimatedReward)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CardWisePalette.Emerald)
            }
            Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium)
            if (recommendation.reward.capped) Text("Reward cap applied", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } }
    }
}

@Composable private fun EmptyState(input: RecommendationInput) { Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) { Column(Modifier.padding(18.dp)) { Text(if (input.amount.isBlank() || input.category.isBlank()) "Add purchase details" else "No eligible card yet", fontWeight = FontWeight.Bold); Text(if (input.amount.isBlank() || input.category.isBlank()) "Enter a positive amount and a category to get a recommendation." else "None of your active cards has a matching reward rule.", Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable private fun ErrorState(message: String, onRetry: () -> Unit) { Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.errorContainer) { Column(Modifier.padding(18.dp)) { Text("Something went wrong", fontWeight = FontWeight.Bold); Text(message, Modifier.padding(top = 5.dp)); TextButton(onClick = onRetry) { Text("Try again") } } } }
