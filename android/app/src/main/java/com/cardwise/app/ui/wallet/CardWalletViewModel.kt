package com.cardwise.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.repository.CardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardWalletViewModel(
    private val repository: CardRepository
) : ViewModel() {
    val uiState: StateFlow<WalletUiState> = repository.observeCards()
        .map<List<Card>, WalletUiState> { WalletUiState.Success(it) }
        .catch { emit(WalletUiState.Error(it.message ?: "Unable to load cards")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WalletUiState.Loading)

    fun addCard(card: Card) {
        viewModelScope.launch { repository.addCard(card) }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch { repository.updateCard(card) }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch { repository.deleteCard(cardId) }
    }
}
