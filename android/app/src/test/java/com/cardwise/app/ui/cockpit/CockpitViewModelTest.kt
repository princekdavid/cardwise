package com.cardwise.app.ui.cockpit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CockpitViewModelTest {
    @Test
    fun `manual calculation is disabled until amount and category are valid`() {
        val viewModel = CockpitViewModel()
        assertFalse(viewModel.uiState.value.canCalculate)

        viewModel.setAmount("0")
        viewModel.setMerchantOrCategory("dining")
        assertFalse(viewModel.uiState.value.canCalculate)

        viewModel.setAmount("2850")
        assertTrue(viewModel.uiState.value.canCalculate)
    }

    @Test
    fun `amount accepts only digits and decimal point`() {
        val viewModel = CockpitViewModel()
        viewModel.setAmount("2850.50")
        assertTrue(viewModel.uiState.value.amount == "2850.50")

        viewModel.setAmount("2850abc")
        assertTrue(viewModel.uiState.value.amount == "2850.50")
    }

    @Test
    fun `category shortcut is normalized`() {
        val viewModel = CockpitViewModel()
        viewModel.selectCategory("Dining")
        assertTrue(viewModel.uiState.value.merchantOrCategory == "dining")
    }
}
