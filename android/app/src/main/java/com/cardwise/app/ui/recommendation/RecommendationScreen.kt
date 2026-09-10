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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.recommendation.CardRecommendation
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.ui.theme.CardWiseMotion
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.PhysicalCard
import com.cardwise.app.ui.theme.SectionTitle
import java.util.Locale

@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel,
    payment: UpiPaymentRequest? = null,
    onContinueToPayment: (() -> Unit)? = null,
    onPaymentInitiated: ((CardRecommendation) -> Unit)? = null,
    onRescan: (() -> Unit)? = null,
    onAdjustDetails: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val input = when (state) {
        is RecommendationUiState.Loading -> (state as RecommendationUiState.Loading).input
        is RecommendationUiState.Ready -> (state as RecommendationUiState.Ready).input
        is RecommendationUiState.Empty -> (state as RecommendationUiState.Empty).input
        is RecommendationUiState.Error -> (state as RecommendationUiState.Error).input
    }
    LazyColumn(modifier.fillMaxSize().testTag("recommendation_content"), contentPadding = PaddingValues(CardWiseSpacing.lg), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)) {
        item { SectionTitle("The best way to pay", "CardWise decision engine", Modifier.semantics { heading() }) }
        payment?.let { item { PaymentSummary(it) } }
        item { InputSection(input, viewModel::setAmount, viewModel::setCategory) }
        when (state) {
            is RecommendationUiState.Loading -> item { DecisionLoader() }
            is RecommendationUiState.Ready -> {
                val ready = state as RecommendationUiState.Ready
                val winner = ready.recommendations.first()
                item { WinnerSpotlight(winner) }
                item { Text("${ready.recommendations.size} ranked match${if (ready.recommendations.size == 1) "" else "es"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(ready.recommendations.drop(1), key = { it.card.id }, contentType = { "recommendation_alternative" }) { RecommendationCard(it, winner) }
                item { RecommendationActions(payment, winner, onContinueToPayment, onPaymentInitiated, onRescan, onAdjustDetails) }
            }
            is RecommendationUiState.Empty -> item { EmptyState((state as RecommendationUiState.Empty).input, onRescan, onAdjustDetails) }
            is RecommendationUiState.Error -> item { ErrorState((state as RecommendationUiState.Error).message, viewModel::retry) }
        }
    }
}

@Composable private fun PaymentSummary(payment: UpiPaymentRequest) {
    GlassCard(elevated = true) { Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
        Text("SCANNED PAYMENT", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
        Text(payment.merchantName ?: "UPI merchant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(payment.vpa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        payment.amount?.let { amount -> Text("₹$amount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CardWisePalette.Emerald, modifier = Modifier.padding(top = CardWiseSpacing.sm)) }
        payment.note?.takeIf(String::isNotBlank)?.let { note -> Text(note, style = MaterialTheme.typography.bodySmall) }
    } }
}

@Composable private fun InputSection(input: RecommendationInput, onAmountChange: (String) -> Unit, onCategoryChange: (String) -> Unit) {
    GlassCard { Column(Modifier.padding(CardWiseSpacing.sm + CardWiseSpacing.xs), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)) {
        OutlinedTextField(value = input.amount, onValueChange = { newValue ->
            val validAmount = newValue.length <= 12 && newValue.count { character -> character == '.' } <= 1 && newValue.all { character -> character.isDigit() || character == '.' }
            if (validAmount) onAmountChange(newValue)
        }, modifier = Modifier.fillMaxWidth().testTag("recommendation_amount"), label = { Text("Amount") }, prefix = { Text("₹ ") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        OutlinedTextField(value = input.category, onValueChange = { newValue -> onCategoryChange(newValue) }, modifier = Modifier.fillMaxWidth().testTag("recommendation_category"), label = { Text("Category") }, placeholder = { Text("Dining, travel, groceries…") }, singleLine = true)
    } }
}

@Composable private fun DecisionLoader() {
    val transition = rememberInfiniteTransition(label = "recommendation_loader")
    val pulseAlpha = transition.animateFloat(0.55f, 1f, infiniteRepeatable(tween(CardWiseMotion.contentTransitionMillis), RepeatMode.Reverse), label = "loader_alpha").value
    GlassCard(elevated = true) { Row(Modifier.padding(CardWiseSpacing.md + CardWiseSpacing.xs), horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.graphicsLayer { alpha = pulseAlpha }, strokeWidth = 3.dp)
        Column { Text("Routing payment matrix…", fontWeight = FontWeight.Bold); Text("Checking benefits, caps and active rules locally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    } }
}

@Composable private fun WinnerSpotlight(recommendation: CardRecommendation) {
    val card = recommendation.card
    AnimatedVisibility(true, enter = fadeIn(tween(CardWiseMotion.contentTransitionMillis)) + slideInVertically(animationSpec = tween(CardWiseMotion.cardEnterMillis)) { it / 5 }) {
        GlassCard(elevated = true, modifier = Modifier.testTag("recommendation_winner")) { Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
            Text("BEST WAY", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
            PhysicalCard(card, compact = false)
            Text("Recommended for this payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("₹${String.format(Locale.ROOT, "%.2f", recommendation.reward.estimatedReward)} expected reward", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CardWisePalette.Emerald)
            Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium)
            Provenance(recommendation)
        } }
    }
}

@Composable private fun RecommendationCard(recommendation: CardRecommendation, winner: CardRecommendation) {
    val card = recommendation.card
    AnimatedVisibility(true, enter = fadeIn(tween(CardWiseMotion.contentTransitionMillis)) + slideInVertically(animationSpec = tween(CardWiseMotion.cardEnterMillis)) { it / 5 }) {
        GlassCard { Column(Modifier.padding(CardWiseSpacing.sm + CardWiseSpacing.xs), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)) {
            PhysicalCard(card, compact = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Alternative ${recommendation.rank - 1}", fontWeight = FontWeight.Bold); Text("${card.issuer} • ${card.network.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("₹${String.format(Locale.ROOT, "%.2f", recommendation.reward.estimatedReward)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CardWisePalette.Emerald)
            }
            Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium)
            Text("Why not this card?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(recommendation.whyNot.ifBlank { "The winner ranks higher on the deterministic reward calculation." }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Provenance(recommendation)
        } }
    }
}

@Composable private fun Provenance(recommendation: CardRecommendation) {
    Surface(Modifier.fillMaxWidth().testTag("recommendation_math_${recommendation.card.id}"), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(CardWiseSpacing.sm), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
            Text("HOW IT WAS CALCULATED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(recommendation.provenance.ifBlank { "Calculated from the active local reward rule." }, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun RecommendationActions(payment: UpiPaymentRequest?, winner: CardRecommendation, onContinueToPayment: (() -> Unit)?, onPaymentInitiated: ((CardRecommendation) -> Unit)?, onRescan: (() -> Unit)?, onAdjustDetails: (() -> Unit)?) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
        if (payment != null && onContinueToPayment != null) Button(onClick = { onPaymentInitiated?.invoke(winner); onContinueToPayment() }, modifier = Modifier.fillMaxWidth().testTag("continue_to_upi")) { Text("Continue to UPI app") }
        if (payment != null && onRescan != null) OutlinedButton(onClick = onRescan, modifier = Modifier.fillMaxWidth().testTag("recommendation_rescan")) { Text("Scan another QR") }
        if (onAdjustDetails != null) TextButton(onClick = onAdjustDetails, modifier = Modifier.fillMaxWidth().testTag("recommendation_adjust")) { Text("Adjust amount or category") }
    }
}

@Composable private fun EmptyState(input: RecommendationInput, onRescan: (() -> Unit)?, onAdjustDetails: (() -> Unit)?) { Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) { Column(Modifier.padding(CardWiseSpacing.md + CardWiseSpacing.xs), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) { Text(if (input.amount.isBlank() || input.category.isBlank()) "Add purchase details" else "No eligible card yet", fontWeight = FontWeight.Bold); Text(if (input.amount.isBlank() || input.category.isBlank()) "Enter a positive amount and a category to get a recommendation." else "None of your active cards has a matching reward rule.", color = MaterialTheme.colorScheme.onSurfaceVariant); if (onRescan != null) OutlinedButton(onClick = onRescan, modifier = Modifier.fillMaxWidth().testTag("recommendation_rescan_empty")) { Text("Scan another QR") }; if (onAdjustDetails != null) TextButton(onClick = onAdjustDetails, modifier = Modifier.fillMaxWidth().testTag("recommendation_adjust_empty")) { Text("Adjust details") } } } }

@Composable private fun ErrorState(message: String, onRetry: () -> Unit) { Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.errorContainer) { Column(Modifier.padding(CardWiseSpacing.md + CardWiseSpacing.xs)) { Text("Something went wrong", fontWeight = FontWeight.Bold); Text(message, Modifier.padding(top = CardWiseSpacing.xs)); TextButton(onClick = onRetry) { Text("Try again") } } } }
