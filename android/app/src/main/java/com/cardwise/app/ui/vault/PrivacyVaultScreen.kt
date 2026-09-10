package com.cardwise.app.ui.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cardwise.app.domain.repository.PrivacyVaultRepository
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PrivacyVaultScreen(
    vaultRepository: PrivacyVaultRepository,
    coroutineScope: CoroutineScope,
    onResetComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetConfirmation by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize().padding(CardWiseSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.md)
    ) {
        Text("PRIVACY VAULT")
        Text("Your data stays yours.", color = CardWisePalette.Emerald)
        Text("CardWise stores the card deck, reward rules, and payment insights locally. Payment handoff sends only the sanitized payment details required to open your selected UPI app.")
        Text("Never stored by CardWise: UPI PINs, banking passwords, OTPs, or account credentials.")
        Text("Resetting the vault permanently clears locally stored CardWise data and returns the app to onboarding.")
        OutlinedButton(
            onClick = { showResetConfirmation = true },
            modifier = Modifier.semantics { contentDescription = "Reset all CardWise data" }
        ) {
            Text("Reset all data")
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset Privacy Vault?") },
            text = { Text("This clears your enrolled cards, reward rules, payment history, and onboarding state from this device. This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    showResetConfirmation = false
                    coroutineScope.launch {
                        vaultRepository.resetAllData()
                        onResetComplete()
                    }
                }) { Text("Reset") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}
