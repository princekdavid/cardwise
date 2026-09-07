package com.cardwise.app.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.validation.CardValidationResult
import com.cardwise.app.domain.validation.CardValidator

@Composable
fun CardFormScreen(
    viewModel: CardWalletViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    existingCard: Card? = null
) {
    var issuer by remember(existingCard) { mutableStateOf(existingCard?.issuer.orEmpty()) }
    var name by remember(existingCard) { mutableStateOf(existingCard?.name.orEmpty()) }
    var lastFour by remember(existingCard) { mutableStateOf(existingCard?.lastFour.orEmpty()) }
    var network by remember(existingCard) { mutableStateOf(existingCard?.network ?: CardNetwork.OTHER) }
    var networkMenuExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (existingCard == null) "Add card" else "Edit card")
        OutlinedTextField(
            value = issuer,
            onValueChange = { issuer = it; error = null },
            label = { Text("Issuer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("card_issuer")
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; error = null },
            label = { Text("Card name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("card_name")
        )
        OutlinedTextField(
            value = lastFour,
            onValueChange = { value ->
                if (value.length <= 4 && value.all(Char::isDigit)) {
                    lastFour = value
                    error = null
                }
            },
            label = { Text("Last four digits") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("card_last_four")
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { networkMenuExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Network: ${network.label}")
            }
            DropdownMenu(
                expanded = networkMenuExpanded,
                onDismissRequest = { networkMenuExpanded = false }
            ) {
                CardNetwork.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            network = option
                            networkMenuExpanded = false
                            error = null
                        }
                    )
                }
            }
        }
        error?.let { Text(it) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onDone) { Text("Cancel") }
            Button(onClick = {
                val card = Card(
                    id = existingCard?.id ?: 0L,
                    issuer = issuer.trim(),
                    name = name.trim(),
                    lastFour = lastFour,
                    network = network,
                    isActive = existingCard?.isActive ?: true,
                    benefits = existingCard?.benefits.orEmpty()
                )
                when (val result = CardValidator().validate(card)) {
                    CardValidationResult.Valid -> {
                        if (existingCard == null) viewModel.addCard(card) else viewModel.updateCard(card)
                        onDone()
                    }
                    is CardValidationResult.Invalid -> {
                        error = result.errors.joinToString(", ") { it.name }
                    }
                }
            }) { Text(if (existingCard == null) "Save card" else "Save changes") }
        }
    }
}

private val CardNetwork.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }
