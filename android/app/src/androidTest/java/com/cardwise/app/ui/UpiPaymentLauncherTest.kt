package com.cardwise.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cardwise.app.domain.scan.UpiPaymentRequest
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpiPaymentLauncherTest {
    private val payment = UpiPaymentRequest(
        vpa = "merchant@upi",
        merchantName = "CardWise Shop",
        amount = BigDecimal("125.00"),
        currency = "INR",
        transactionReference = "ref-123",
        note = "Order 42"
    )

    @Test
    fun launch_usesChooserWithSanitizedUpiViewIntent() {
        val context = RecordingContext(InstrumentationRegistry.getInstrumentation().targetContext)
        val result = AndroidUpiPaymentLauncher(context).launch(payment)

        assertEquals(UpiPaymentLaunchResult.Launched, result)
        val chooser = context.startedIntent
        assertNotNull(chooser)
        assertEquals(Intent.ACTION_CHOOSER, chooser!!.action)

        val target = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(target)
        assertEquals(Intent.ACTION_VIEW, target!!.action)
        assertEquals(
            "upi://pay?pa=merchant%40upi&pn=CardWise+Shop&am=125&cu=INR&tr=ref-123&tn=Order+42",
            target.dataString
        )
    }

    @Test
    fun launch_whenNoActivityAvailable_returnsNoUpiApp() {
        val context = ThrowingContext(InstrumentationRegistry.getInstrumentation().targetContext)

        val result = AndroidUpiPaymentLauncher(context).launch(payment)

        assertEquals(UpiPaymentLaunchResult.NoUpiApp, result)
    }

    @Test
    fun launch_withUnsafePayment_doesNotStartActivity() {
        val context = RecordingContext(InstrumentationRegistry.getInstrumentation().targetContext)
        val unsafe = payment.copy(note = "bad\nvalue")

        val result = AndroidUpiPaymentLauncher(context).launch(unsafe)

        assertEquals(UpiPaymentLaunchResult.UnsafePayment, result)
        assertTrue(context.startedIntent == null)
    }

    private class RecordingContext(base: Context) : ContextWrapper(base) {
        var startedIntent: Intent? = null
            private set

        override fun startActivity(intent: Intent) {
            startedIntent = intent
        }
    }

    private class ThrowingContext(base: Context) : ContextWrapper(base) {
        override fun startActivity(intent: Intent) {
            throw ActivityNotFoundException("No handler")
        }
    }
}
