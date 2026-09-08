package com.cardwise.app.ui.cockpit

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CockpitUiState(
    val amount: String = "",
    val merchantOrCategory: String = ""
) {
    val canCalculate: Boolean
        get() = amount.toBigDecimalOrNull()?.let { it > java.math.BigDecimal.ZERO } == true && merchantOrCategory.isNotBlank()
}

class CockpitViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CockpitUiState())
    val uiState: StateFlow<CockpitUiState> = _uiState.asStateFlow()

    fun setAmount(value: String) {
        if (value.length <= 10 && value.all { it.isDigit() || it == '.' }) {
            _uiState.value = _uiState.value.copy(amount = value)
        }
    }

    fun setMerchantOrCategory(value: String) {
        _uiState.value = _uiState.value.copy(merchantOrCategory = value)
    }

    fun selectCategory(value: String) = setMerchantOrCategory(value.lowercase())
}
