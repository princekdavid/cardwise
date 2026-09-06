package com.cardwise.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.cardwise.app.domain.scan.UpiPaymentHandoff
import com.cardwise.app.domain.scan.UpiPaymentRequest

sealed interface UpiPaymentLaunchResult {
    data object Launched : UpiPaymentLaunchResult
    data object UnsafePayment : UpiPaymentLaunchResult
    data object NoUpiApp : UpiPaymentLaunchResult
}

interface UpiPaymentLauncher {
    fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult
}

class AndroidUpiPaymentLauncher(
    private val context: Context
) : UpiPaymentLauncher {
    override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult {
        val uri = runCatching { UpiPaymentHandoff.buildUri(payment) }.getOrNull()
            ?: return UpiPaymentLaunchResult.UnsafePayment

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(Intent.createChooser(intent, "Choose UPI app"))
            UpiPaymentLaunchResult.Launched
        } catch (_: ActivityNotFoundException) {
            UpiPaymentLaunchResult.NoUpiApp
        }
    }
}
