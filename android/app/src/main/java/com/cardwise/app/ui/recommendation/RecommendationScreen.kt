package com.cardwise.app.ui.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.recommendation.CardRecommendation
import com.cardwise.app.ui.theme.CardWiseSpacing
import java.util.Locale

@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(CardWiseSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.md)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                Text("Smart payment", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Enter the purchase and CardWise will rank your configured cards by estimated reward.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            InputSection(
                input = when (state) {
                    RecommendationUiState.Loading -> RecommendationInput()
                    is RecommendationUiState.Ready -> (state as RecommendationUiState.Ready).input
                    is RecommendationUiState.Empty -> (state as RecommendationUiState.Empty).input
                    is RecommendationUiState.Error -> (state as RecommendationUiState.Error).input
                },
                onAmountChange = viewModel::setAmount,
                onCategoryChange = viewModel::setCategory
            )
        }

        when (state) {
            RecommendationUiState.Loading -> item { LoadingState() }
            is RecommendationUiState.Ready -> {
                val ready = state as RecommendationUiState.Ready
                item { SectionHeader(ready.recommendations.size) }
                items(ready.recommendations, key = { it.card.id }) { recommendation ->
                    RecommendationCard(recommendation)
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
                    if (value.length <= 12 && value.all { it.isDigit() || it == '.' }) {
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
                        text = if (recommendation.rank == 1) "Recommended" else "Option ${recommendation.rank}",
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
            Text(
                recommendation.reason,
                style = MaterialTheme.typography.bodyMedium
            )
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
