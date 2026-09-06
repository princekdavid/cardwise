package com.cardwise.app.ui.recommendation

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.recommendation.CardRecommendation
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.ui.theme.CardWiseSpacing
import java.util.Locale

@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel,
    payment: UpiPaymentRequest? = null,
    onContinueToPayment: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val input = when (state) {
        is RecommendationUiState.Loading -> (state as RecommendationUiState.Loading).input
        is RecommendationUiState.Ready -> (state as RecommendationUiState.Ready).input
        is RecommendationUiState.Empty -> (state as RecommendationUiState.Empty).input
        is RecommendationUiState.Error -> (state as RecommendationUiState.Error).input
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(CardWiseSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.md)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                Text("Smart payment", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (payment != null) "We found your payment QR. CardWise ranks your configured cards locally, then you choose the UPI app." 
                    else "Enter the purchase and CardWise will rank your configured cards by estimated reward.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (payment != null) {
            item { PaymentSummary(payment) }
        }

        item {
            InputSection(
                input = input,
                onAmountChange = viewModel::setAmount,
                onCategoryChange = viewModel::setCategory
            )
        }

        when (state) {
            is RecommendationUiState.Loading -> item { LoadingState() }
            is RecommendationUiState.Ready -> {
                val ready = state as RecommendationUiState.Ready
                item { SectionHeader(ready.recommendations.size) }
                items(ready.recommendations, key = { it.card.id }) { recommendation ->
                    RecommendationCard(recommendation)
                }
                if (payment != null && onContinueToPayment != null) {
                    item {
                        Button(
                            onClick = onContinueToPayment,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue to UPI app")
                        }
                    }
                }
            }
            is RecommendationUiState.Empty -> item {
                EmptyState((state as RecommendationUiState.Empty).input)
            }
            is RecommendationUiState.Error -> item {
                ErrorState(
                    message = (state as RecommendationUiState.Error).message,
                    onRetry = viewModel::retry
                )
            }
        }
    }
}

@Composable
private fun PaymentSummary(payment: UpiPaymentRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(CardWiseSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)
        ) {
            Text("Scanned payment", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(payment.merchantName ?: "UPI merchant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(payment.vpa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            payment.amount?.let {
                Text(
                    "₹${it.toPlainString()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = CardWiseSpacing.xs)
                )
            }
            payment.note?.takeIf(String::isNotBlank)?.let {
                Text("Note: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun InputSection(
    input: RecommendationInput,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(CardWiseSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)
        ) {
            OutlinedTextField(
                value = input.amount,
                onValueChange = { value ->
                    if (value.length <= 12 && value.count { it == '.' } <= 1 && value.all { it.isDigit() || it == '.' }) {
                        onAmountChange(value)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                prefix = { Text("₹ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = input.category,
                onValueChange = onCategoryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Category") },
                placeholder = { Text("Dining, travel, groceries…") },
                singleLine = true
            )
        }
    }
}

@Composable
private fun SectionHeader(count: Int) {
    Text(
        text = if (count == 1) "Best match" else "$count matches",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun RecommendationCard(recommendation: CardRecommendation) {
    val card = recommendation.card
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (recommendation.rank == 1) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(CardWiseSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (recommendation.rank == 1) "Recommended for this payment" else "Alternative ${recommendation.rank - 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(card.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${card.issuer} • ${card.network.name} •••• ${card.lastFour}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "₹${String.format(Locale.ROOT, "%.2f", recommendation.reward.estimatedReward)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("estimated benefit", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium)
                if (recommendation.reward.capped) {
                    Text(
                        "Reward cap applied",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(CardWiseSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
            Text("Checking your cards…", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun EmptyState(input: RecommendationInput) {
    val needsInput = input.amount.isBlank() || input.category.isBlank()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(CardWiseSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)
        ) {
            Text(
                if (needsInput) "Add purchase details" else "No eligible card yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (needsInput) {
                    "Enter a positive amount and a merchant category to get a recommendation."
                } else {
                    "None of your active cards has a matching reward rule for this purchase."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.padding(CardWiseSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)
        ) {
            Text("Something went wrong", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Try again") }
        }
    }
}
