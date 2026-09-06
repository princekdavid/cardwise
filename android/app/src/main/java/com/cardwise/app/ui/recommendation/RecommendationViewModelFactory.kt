package com.cardwise.app.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.rewards.RewardRule

class RecommendationViewModelFactory(
    private val repository: CardRepository,
    private val rewardRuleRepository: RewardRuleRepository? = null,
    private val rules: Map<Long, List<RewardRule>> = emptyMap()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return RecommendationViewModel(repository, rewardRuleRepository, rules) as T
    }
}
