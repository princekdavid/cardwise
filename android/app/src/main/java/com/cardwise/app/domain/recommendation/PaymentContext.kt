package com.cardwise.app.domain.recommendation

/** Context used to determine the best eligible payment card. */
data class PaymentContext(
    val category: String,
    val amount: Double,
    val currency: String = "INR"
) {
    init {
        require(category.isNotBlank()) { "Category must not be blank" }
        require(amount.isFinite() && amount >= 0.0) { "Amount must be a finite, non-negative value" }
        require(currency.isNotBlank()) { "Currency must not be blank" }
    }
}
