package com.cardwise.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cardwise.app.domain.repository.CardRepository

class CardWalletViewModelFactory(
    private val repository: CardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CardWalletViewModel::class.java))
        return CardWalletViewModel(repository) as T
    }
}
