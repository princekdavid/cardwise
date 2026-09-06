package com.cardwise.app.domain.validation

import com.cardwise.app.domain.model.Card

class CardValidator {
    fun validate(card: Card): CardValidationResult {
        val errors = buildList {
            if (card.issuer.isBlank()) add(CardValidationError.IssuerRequired)
            if (card.name.isBlank()) add(CardValidationError.NameRequired)
            if (!card.lastFour.matches(FOUR_DIGITS)) add(CardValidationError.LastFourInvalid)
            if (card.benefits.any { it.title.isBlank() || it.description.isBlank() }) {
                add(CardValidationError.BenefitIncomplete)
            }
        }
        return if (errors.isEmpty()) {
            CardValidationResult.Valid
        } else {
            CardValidationResult.Invalid(errors)
        }
    }

    private companion object {
        val FOUR_DIGITS = Regex("\\d{4}")
    }
}

enum class CardValidationError {
    IssuerRequired,
    NameRequired,
    LastFourInvalid,
    BenefitIncomplete
}

sealed interface CardValidationResult {
    data object Valid : CardValidationResult
    data class Invalid(val errors: List<CardValidationError>) : CardValidationResult
}
