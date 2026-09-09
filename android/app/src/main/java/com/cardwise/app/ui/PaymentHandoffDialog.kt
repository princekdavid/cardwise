package com.cardwise.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.ui.theme.CardWisePalette
import com.cardwise.app.ui.theme.CardWiseSpacing
import java.util.Locale

@Composable
fun PaymentHandoffDialog(
    payment: UpiPaymentRequest,
    launcher: UpiPaymentLauncher,
    onDismiss: () -> Unit,
    onHandoffCompleted: (UpiPaymentLaunchResult) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                Text("READY TO PAY", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald, fontWeight = FontWeight.Bold)
                Text("Continue to your UPI app?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.md)) {
                Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                    Text(payment.merchantName?.takeIf { it.isNotBlank() } ?: "UPI merchant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(payment.vpa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    payment.amount?.let {
                        Text("₹${String.format(Locale.ROOT, "%.2f", it)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    } ?: Text("Amount will be entered in the UPI app", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(verticalArrangement = Arrangement.spacedBy(CardWiseSpacing.xs)) {
                    Text("Payment handoff", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "CardWise will pass only sanitized payment details. Choose the UPI app on your device and complete payment there.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(Modifier.fillMaxWidth()) {
                    Text("No PIN or banking credentials are shared by CardWise.", style = MaterialTheme.typography.labelSmall, color = CardWisePalette.Emerald)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                val result = launcher.launch(payment)
                onHandoffCompleted(result)
                when (result) {
                    UpiPaymentLaunchResult.Launched -> Unit
                    UpiPaymentLaunchResult.UnsafePayment -> Toast.makeText(context, "This payment can't be handed off safely.", Toast.LENGTH_SHORT).show()
                    UpiPaymentLaunchResult.NoUpiApp -> Toast.makeText(context, "No UPI app is available on this device.", Toast.LENGTH_LONG).show()
                }
            }) { Text("Choose UPI app") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
