package com.cardwise.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardwise.app.CardWiseApplication
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.navigation.AppDestination
import com.cardwise.app.ui.recommendation.RecommendationScreen
import com.cardwise.app.ui.recommendation.RecommendationViewModel
import com.cardwise.app.ui.recommendation.RecommendationViewModelFactory
import com.cardwise.app.ui.theme.CardWiseMotion
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.CardWiseTheme
import com.cardwise.app.ui.wallet.CardDetailScreen
import com.cardwise.app.ui.wallet.CardFormScreen
import com.cardwise.app.ui.wallet.CardWalletScreen
import com.cardwise.app.ui.wallet.CardWalletViewModel
import com.cardwise.app.ui.wallet.CardWalletViewModelFactory
import com.cardwise.app.ui.wallet.WalletUiState

private enum class WalletScreen { List, Add, Detail, Edit }

@Composable
fun CardWiseApp(
    repository: CardRepository? = null,
    recommendationRules: Map<Long, List<RewardRule>> = emptyMap()
) {
    CardWiseTheme {
        var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
        var walletScreen by rememberSaveable { mutableStateOf(WalletScreen.List) }
        var selectedCardId by rememberSaveable { mutableStateOf<Long?>(null) }
        val destination = AppDestination.entries[selectedIndex]
        val resolvedRepository = repository ?: (LocalContext.current.applicationContext as CardWiseApplication)
            .container.cardRepository

        val walletViewModel: CardWalletViewModel = viewModel(
            factory = remember(resolvedRepository) { CardWalletViewModelFactory(resolvedRepository) }
        )
        val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
        val selectedCard = (walletState as? WalletUiState.Success)
            ?.cards?.firstOrNull { it.id == selectedCardId }

        val recommendationViewModel: RecommendationViewModel = viewModel(
            key = "recommendation",
            factory = remember(resolvedRepository, recommendationRules) {
                RecommendationViewModelFactory(resolvedRepository, recommendationRules)
            }
        )

        Scaffold(
            bottomBar = {
                if (walletScreen == WalletScreen.List) {
                    NavigationBar {
                        AppDestination.entries.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = index == selectedIndex,
                                onClick = { selectedIndex = index },
                                icon = { Text(item.label.take(1)) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = walletScreen to destination,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                transitionSpec = {
                    fadeIn(tween(CardWiseMotion.screenTransitionMillis)) togetherWith
                        fadeOut(tween(CardWiseMotion.screenTransitionMillis))
                },
                label = "app_screen_transition"
            ) { (screen, currentDestination) ->
                if (currentDestination == AppDestination.Insights) {
                    RecommendationScreen(viewModel = recommendationViewModel)
                } else if (currentDestination != AppDestination.Wallet) {
                    Text(
                        text = currentDestination.label,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(CardWiseSpacing.lg)
                    )
                } else {
                    when (screen) {
                        WalletScreen.List -> CardWalletScreen(
                            viewModel = walletViewModel,
                            onAddCard = { walletScreen = WalletScreen.Add },
                            onOpenCard = { card ->
                                selectedCardId = card.id
                                walletScreen = WalletScreen.Detail
                            }
                        )
                        WalletScreen.Add -> CardFormScreen(
                            viewModel = walletViewModel,
                            onDone = { walletScreen = WalletScreen.List }
                        )
                        WalletScreen.Detail -> if (selectedCard != null) {
                            CardDetailScreen(
                                card = selectedCard,
                                onEdit = { walletScreen = WalletScreen.Edit },
                                onDelete = {
                                    walletViewModel.deleteCard(selectedCard.id)
                                    selectedCardId = null
                                    walletScreen = WalletScreen.List
                                },
                                onBack = { walletScreen = WalletScreen.List }
                            )
                        } else {
                            Text("Card not found", modifier = Modifier.padding(CardWiseSpacing.lg))
                        }
                        WalletScreen.Edit -> if (selectedCard != null) {
                            CardFormScreen(
                                viewModel = walletViewModel,
                                existingCard = selectedCard,
                                onDone = { walletScreen = WalletScreen.Detail }
                            )
                        } else {
                            Text("Card not found", modifier = Modifier.padding(CardWiseSpacing.lg))
                        }
                    }
                }
            }
        }
    }
}
