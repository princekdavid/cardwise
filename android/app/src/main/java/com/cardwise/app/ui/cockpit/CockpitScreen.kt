package com.cardwise.app.ui.cockpit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardwise.app.domain.model.Card
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.DecisionPulse
import com.cardwise.app.ui.theme.EnginePulse
import com.cardwise.app.ui.theme.GlassCard
import com.cardwise.app.ui.theme.SectionTitle

@Composable
fun CockpitScreen(
    cards: List<Card>,
    onScan: () -> Unit,
    onCalculate: (amount: String, category: String) -> Unit,
    onOpenCards: () -> Unit,
    onToggleTheme: () -> Unit,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    val shortcuts = listOf("Dining", "Travel", "Shopping", "Groceries")

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = CardWisePalette.Emerald.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "CW",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        color = CardWisePalette.Emerald,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EnginePulse()
                        Text(
                            "CARDWISE ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CardWisePalette.Emerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Payment Cockpit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onToggleTheme) {
                Text(if (darkTheme) "☼" else "☾", style = MaterialTheme.typography.titleLarge)
            }
        }

        AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn()) {
            GlassCard(elevated = true, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricTile("${cards.count { it.isActive }}", "Active cards", Modifier.weight(1f))
                        MetricTile("Local", "Decision engine", Modifier.weight(1f))
                    }
                }
            }
        }

        GlassCard(elevated = true, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("POINT & PAY", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                Text("Scan a merchant QR", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Detect the payee, amount and payment context, then find the highest-value route from your deck.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) {
                    Text("Scan & Find")
                }
            }
        }

        SectionTitle("Choose a route", "Manual calculation")
        GlassCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() || c == '.' }) amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("₹ ") },
                    label = { Text("Amount") }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Merchant or category") },
                    placeholder = { Text("e.g. dining") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    shortcuts.forEach { shortcut ->
                        FilterChip(
                            selected = category.equals(shortcut, true),
                            onClick = { category = shortcut.lowercase() },
                            label = { Text(shortcut) }
                        )
                    }
                }
                Button(
                    onClick = { onCalculate(amount, category) },
                    enabled = amount.isNotBlank() && category.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Find the best way")
                }
            }
        }

        GlassCard(elevated = true) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DecisionPulse(Modifier.size(46.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Build your Card Deck", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Add cards from the catalogue to unlock better recommendations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onOpenCards) { Text("Open deck") }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun MetricTile(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
