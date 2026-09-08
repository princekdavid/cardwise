package com.cardwise.app.ui.catalog

import com.cardwise.app.domain.model.Card
import com.cardwise.app.domain.model.CardBenefit
import com.cardwise.app.domain.model.CardNetwork

val CardWiseCatalogue = listOf(
    Card(1001L, "Scapia Federal Bank", "Scapia Travel Card", "8821", CardNetwork.RUPAY, benefits = listOf(CardBenefit("Travel rewards", "Travel-focused rewards and benefits", "travel"))),
    Card(1002L, "ICICI Bank", "Amazon Pay ICICI", "4412", CardNetwork.VISA, benefits = listOf(CardBenefit("Amazon rewards", "Higher value on eligible Amazon purchases", "shopping"))),
    Card(1003L, "Slice", "Slice Super Card", "2219", CardNetwork.VISA, benefits = listOf(CardBenefit("Daily UPI", "Cashback on eligible UPI spends", "upi"))),
    Card(1004L, "HDFC Bank", "HDFC Millennia", "7345", CardNetwork.VISA, benefits = listOf(CardBenefit("Online shopping", "Accelerated rewards on eligible online merchants", "shopping"))),
    Card(1005L, "HDFC Bank", "HDFC Swiggy", "5108", CardNetwork.MASTERCARD, benefits = listOf(CardBenefit("Food & grocery", "Accelerated rewards on eligible food and grocery spends", "dining"))),
    Card(1006L, "IndusInd Bank", "IndusInd Tiger", "9630", CardNetwork.VISA, benefits = listOf(CardBenefit("Lifestyle", "Lifestyle and travel benefits", "lifestyle")))
)
