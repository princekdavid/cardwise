package com.cardwise.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(CardWiseSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CARDWISE")
        Text("Your cards. Your decisions. Private by design.", modifier = Modifier.padding(top = CardWiseSpacing.sm))
        Text(
            "CardWise keeps your card deck, payment history, and recommendation inputs on this device. It does not need your banking password, PIN, or UPI credentials.",
            modifier = Modifier.padding(top = CardWiseSpacing.lg)
        )
        Text(
            "Privacy oath",
            color = CardWisePalette.QuantumEmerald,
            modifier = Modifier.padding(top = CardWiseSpacing.lg)
        )
        Text(
            "No credentials. No PINs. No silent sharing.",
            modifier = Modifier.padding(top = CardWiseSpacing.sm)
        )
        Button(
            onClick = onContinue,
            modifier = Modifier.padding(top = CardWiseSpacing.xl).semantics { contentDescription = "Accept privacy oath" }
        ) {
            Text("I understand — continue")
        }
    }
}
