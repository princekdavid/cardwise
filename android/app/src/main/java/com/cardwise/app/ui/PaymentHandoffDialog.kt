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
        title = { Text("Continue to your UPI app?") },
        text = {
            Text("CardWise will pass only sanitized payment details to a UPI app. You will choose the app and complete payment there.")
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
            }) { Text("Continue") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
