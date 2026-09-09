package com.cardwise.app.domain.recommendation

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.rewards.RewardEstimate

data class CardRecommendation(
    val card: Card,
    val reward: RewardEstimate,
    val reason: String,
    val rank: Int,
    val provenance: String = "",
    val whyNot: String = ""
)
