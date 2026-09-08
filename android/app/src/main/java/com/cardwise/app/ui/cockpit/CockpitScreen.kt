package com.cardwise.app.ui.cockpit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardwise.app.domain.model.Card
import com.cardwise.app.ui.theme.CardWiseMotion
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.DecisionPulse
import com.cardwise.app.ui.theme.EnginePulse
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.MetricTile
import com.cardwise.app.ui.theme.SectionTitle

@Composable
fun CockpitScreen(
    cards: List<Card>,
    viewModel: CockpitViewModel,
    onScan: () -> Unit,
    onCalculate: (amount: String, category: String) -> Unit,
    onOpenCards: () -> Unit,
    onToggleTheme: () -> Unit,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val shortcuts = listOf("Dining", "Travel", "Shopping", "Groceries")

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CardWiseSpacing.lg, vertical = CardWiseSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.md)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                    EnginePulse()
                    Text("CARDWISE ENGINE", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                }
                Text("Payment Cockpit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(50)) {
                    Text(
                        "${cards.size} cards",
                        modifier = Modifier.padding(horizontal = CardWiseSpacing.sm, vertical = CardWiseSpacing.xs),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onToggleTheme, modifier = Modifier.semantics { contentDescription = "Toggle theme" }) {
                    Text(if (darkTheme) "☼" else "☾", style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(CardWiseMotion.contentTransitionMillis)) + scaleIn(tween(CardWiseMotion.emphasisTransitionMillis))
        ) {
            GlassCard(elevated = true, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                            Text("YOUR MONEY, OPTIMIZED", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                            Text("Make every payment count.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "CardWise compares your deck before you pay — so you don't have to.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DecisionPulse(Modifier.size(48.dp))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
                        MetricTile("${cards.count { it.isActive }}", "Active cards", Modifier.weight(1f))
                        MetricTile("Local", "Decision engine", Modifier.weight(1f))
                    }
                }
            }
        }

        GlassCard(elevated = true, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(CardWiseSpacing.md), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
                Text("POINT & PAY", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                Text("Scan a merchant QR", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Detect the payee, amount and payment context, then find the highest-value route from your deck.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onScan, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Scan merchant QR" }) { Text("Scan & Find") }
            }
        }

        SectionTitle("Choose a route", "Manual calculation")
        GlassCard {
            Column(Modifier.padding(CardWiseSpacing.sm + CardWiseSpacing.xs), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)) {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = viewModel::setAmount,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("₹ ") },
                    label = { Text("Amount") }
                )
                OutlinedTextField(
                    value = state.merchantOrCategory,
                    onValueChange = viewModel::setMerchantOrCategory,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Merchant or category") },
                    placeholder = { Text("e.g. dining") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm)) {
                    shortcuts.forEach { shortcut ->
                        FilterChip(
                            selected = state.merchantOrCategory.equals(shortcut, true),
                            onClick = { viewModel.selectCategory(shortcut) },
                            label = { Text(shortcut) }
                        )
                    }
                }
                Button(
                    onClick = { onCalculate(state.amount, state.merchantOrCategory) },
                    enabled = state.canCalculate,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Find the best way") }
            }
        }

        GlassCard(elevated = true) {
            Row(Modifier.padding(CardWiseSpacing.sm + CardWiseSpacing.xs), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CardWiseSpacing.sm + CardWiseSpacing.xs)) {
                DecisionPulse(Modifier.size(46.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                    Text("Build your Card Deck", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        if (cards.isEmpty()) "Add cards from the catalogue to start optimizing payments."
                        else "Your deck is ready. Add more cards to broaden recommendations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onOpenCards) { Text("Open deck") }
            }
        }
        Spacer(Modifier.height(CardWiseSpacing.xs))
    }
}
