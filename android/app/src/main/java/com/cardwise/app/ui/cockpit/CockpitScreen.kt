package com.cardwise.app.ui.cockpit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Column(modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnginePulse()
                Column {
                    Text("CARDWISE ENGINE", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                    Text("Payment Cockpit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onToggleTheme) { Text(if (darkTheme) "☼" else "☾", style = MaterialTheme.typography.titleLarge) }
        }

        AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn()) {
            GlassCard(elevated = true, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("POINT & PAY", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                            Text("Scan merchant QR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Detect the payee and find the highest-yield route from your deck.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(onClick = onScan, color = CardWisePalette.Emerald, shape = RoundedCornerShape(16.dp)) {
                            Text("⌁", modifier = Modifier.padding(14.dp), color = CardWisePalette.Obsidian, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassCard(Modifier.weight(1f)) { Column(Modifier.padding(14.dp)) { Text("${cards.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Cards in deck", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            GlassCard(Modifier.weight(1f)) { Column(Modifier.padding(14.dp)) { Text("Local", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Decision engine", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }

        SectionTitle("Calculate a purchase", "Manual route")
        GlassCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = amount, onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() || c == '.' }) amount = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text("₹ ") }, label = { Text("Amount") })
                OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Merchant / category") }, placeholder = { Text("e.g. dining") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { shortcuts.forEach { FilterChip(selected = category.equals(it, true), onClick = { category = it.lowercase() }, label = { Text(it) }) } }
                Button(onClick = { onCalculate(amount, category) }, enabled = amount.isNotBlank() && category.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Find the best way") }
            }
        }

        GlassCard(elevated = true) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DecisionPulse(Modifier.size(54.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Your deck, optimized", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Add cards from the catalogue so recommendations become more useful.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onOpenCards) { Text("Cards") }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
