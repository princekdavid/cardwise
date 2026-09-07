package com.cardwise.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.domain.scan.UpiPaymentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal

class CardWiseFlowActivity : ComponentActivity() {
    companion object {
        lateinit var launcher: TestPaymentLauncher
            private set
    }

    private lateinit var repository: TestCardRepository
    private lateinit var card: Card
    private var initialPayment by mutableStateOf<UpiPaymentRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = TestPaymentLauncher()
        card = Card(
            id = 1L,
            issuer = "CardWise Bank",
            name = "Everyday Rewards",
            lastFour = "1234",
            network = CardNetwork.VISA
        )
        repository = TestCardRepository(emptyList())
        setContent {
            CardWiseApp(
                repository = repository,
                recommendationRules = mapOf(
                    card.id to listOf(RewardRule("dining", rewardRatePercent = 5.0))
                ),
                paymentLauncher = launcher,
                initialPayment = initialPayment
            )
        }
    }

    fun showScannedPayment() {
        initialPayment = paymentFixture()
    }

    private fun paymentFixture() = UpiPaymentRequest(
        vpa = "merchant@upi",
        merchantName = "CardWise Shop",
        amount = BigDecimal("125.00"),
        currency = "INR",
        transactionReference = "ref-123",
        note = "Order 42"
    )
}

private class TestCardRepository(initialCards: List<Card>) : CardRepository {
    private val cards = MutableStateFlow(initialCards)

    override fun observeCards(): Flow<List<Card>> = cards

    override suspend fun addCard(card: Card): Long {
        val savedCard = card.copy(id = if (card.id == 0L) 1L else card.id)
        cards.value = cards.value + savedCard
        return savedCard.id
    }

    override suspend fun updateCard(card: Card) {
        cards.value = cards.value.map { if (it.id == card.id) card else it }
    }

    override suspend fun deleteCard(cardId: Long) {
        cards.value = cards.value.filterNot { it.id == cardId }
    }
}

class TestPaymentLauncher : UpiPaymentLauncher {
    var launchCount = 0
        private set

    override fun launch(payment: UpiPaymentRequest): UpiPaymentLaunchResult {
        launchCount += 1
        return UpiPaymentLaunchResult.Launched
    }
}
