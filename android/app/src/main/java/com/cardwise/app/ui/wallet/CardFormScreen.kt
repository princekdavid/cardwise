package com.cardwise.app.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.validation.CardValidationResult
import com.cardwise.app.domain.validation.CardValidator

@Composable
fun CardFormScreen(
    viewModel: CardWalletViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var issuer by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var lastFour by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add card")
        OutlinedTextField(
            value = issuer,
            onValueChange = { issuer = it; error = null },
            label = { Text("Issuer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; error = null },
            label = { Text("Card name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier.fillMaxWidth()
        )
        error?.let { Text(it) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onDone) { Text("Cancel") }
            Button(onClick = {
                val card = Card(0L, issuer.trim(), name.trim(), lastFour, CardNetwork.OTHER)
                when (val result = CardValidator().validate(card)) {
                    CardValidationResult.Valid -> {
                        viewModel.addCard(card)
                        onDone()
                    }
                    is CardValidationResult.Invalid -> {
                        error = result.errors.joinToString(", ") { it.name }
                    }
                }
            }) { Text("Save card") }
        }
    }
}
