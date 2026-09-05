package com.cardwise.app.ui.wallet

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardNetwork
import com.cardwise.app.domain.repository.CardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CardWalletViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addCard_delegatesToRepository() = runTest {
        val repository = FakeCardRepository()
        val viewModel = CardWalletViewModel(repository)
        val card = card(1L)

        viewModel.addCard(card)
        advanceUntilIdle()

        assertEquals(listOf(card), repository.cards.value)
    }

    @Test
    fun updateCard_delegatesToRepository() = runTest {
        val existing = card(1L)
        val repository = FakeCardRepository(existing)
        val viewModel = CardWalletViewModel(repository)
        val updated = existing.copy(name = "Travel")

        viewModel.updateCard(updated)
        advanceUntilIdle()

        assertEquals(listOf(updated), repository.cards.value)
    }

    @Test
    fun deleteCard_delegatesToRepository() = runTest {
        val repository = FakeCardRepository(card(1L), card(2L))
        val viewModel = CardWalletViewModel(repository)

        viewModel.deleteCard(1L)
        advanceUntilIdle()

        assertEquals(listOf(card(2L)), repository.cards.value)
    }

    @Test
    fun uiState_reflectsRepositoryFlow() = runTest {
        val card = card(3L)
        val repository = FakeCardRepository()
        val viewModel = CardWalletViewModel(repository)

        repository.cards.value = listOf(card)
        advanceUntilIdle()

        assertEquals(WalletUiState.Success(listOf(card)), viewModel.uiState.first())
    }

    private fun card(id: Long) = Card(id, "Bank", "Rewards", "1234", CardNetwork.VISA)
}

private class FakeCardRepository(vararg initialCards: Card) : CardRepository {
    val cards = MutableStateFlow(initialCards.toList())

    override fun observeCards(): Flow<List<Card>> = cards

    override suspend fun addCard(card: Card): Long {
        val id = if (card.id == 0L) (cards.value.maxOfOrNull { it.id } ?: 0L) + 1L else card.id
        cards.value = cards.value + card.copy(id = id)
        return id
    }

    override suspend fun updateCard(card: Card) {
        cards.value = cards.value.map { if (it.id == card.id) card else it }
    }

    override suspend fun deleteCard(cardId: Long) {
        cards.value = cards.value.filterNot { it.id == cardId }
    }
}
