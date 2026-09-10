package com.cardwise.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.SectionTitle
import java.util.Locale

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("insights_content"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(CardWiseSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)
    ) {
        item { SectionTitle("Insights", "Your CardWise value, explained") }
        when (state) {
            InsightsUiState.Empty -> item {
                GlassCard(elevated = true) {
                    Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                        Text("Build your history", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Your savings, payment trends and milestones will appear here after your first payment handoff.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is InsightsUiState.Content -> {
                val summary = (state as InsightsUiState.Content).summary
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
                        MetricCard("Optimized reward", "₹${String.format(Locale.ROOT, "%.2f", summary.totalReward)}", Modifier.weight(1f).testTag("insights_total_reward"))
                        MetricCard("Payment count", summary.paymentCount.toString(), Modifier.weight(1f).testTag("insights_payment_count"))
                    }
                }
                item { Text("Category trends", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                items(summary.categoryRewardTotals, key = { it.first }) { (category, reward) ->
                    GlassCard {
                        Row(Modifier.fillMaxWidth().padding(CardWiseSpacing.md), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(category.replace('_', ' '), fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.ROOT, "%.2f", reward)}", color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { Text("Milestones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                items(summary.milestones, key = { it.id }) { milestone ->
                    GlassCard {
                        Column(Modifier.fillMaxWidth().padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                            Text(milestone.title, fontWeight = FontWeight.Bold)
                            Text(if (milestone.completed) "Completed" else "${milestone.progress} / ${milestone.target}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CardWisePalette.Emerald)
        }
    }
}
