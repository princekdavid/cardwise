package com.cardwise.app.ui

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cardwise.app.domain.scan.UpiPaymentRequest

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
        title = { Text("Choose a UPI app") },
        text = {
            Text(
                "CardWise will pass only sanitized payment details. If multiple UPI apps are installed, Android will let you choose one. CardWise's recommended card or payment method is a suggestion; the selected UPI app controls the final card or payment method."
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                val result = launcher.launch(payment)
                onHandoffCompleted(result)
                when (result) {
                    UpiPaymentLaunchResult.Launched -> Unit
                    UpiPaymentLaunchResult.UnsafePayment ->
                        Toast.makeText(
                            context,
                            "This payment can't be handed off safely.",
                            Toast.LENGTH_SHORT
                        ).show()
                    UpiPaymentLaunchResult.NoUpiApp ->
                        Toast.makeText(
                            context,
                            "No UPI app is available on this device.",
                            Toast.LENGTH_LONG
                        ).show()
                }
            }) { Text("Choose app") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
