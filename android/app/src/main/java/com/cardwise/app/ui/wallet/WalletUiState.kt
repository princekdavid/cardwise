package com.cardwise.app.ui.wallet

import com.cardwise.app.domain.model.Card

sealed interface WalletUiState {
    data object Loading : WalletUiState
    data class Success(val cards: List<Card>) : WalletUiState
    data class Error(val message: String) : WalletUiState
}
