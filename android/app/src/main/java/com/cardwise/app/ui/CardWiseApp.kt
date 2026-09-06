package com.cardwise.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.navigation.AppDestination
import com.cardwise.app.ui.recommendation.RecommendationScreen
import com.cardwise.app.ui.recommendation.RecommendationViewModel
import com.cardwise.app.ui.recommendation.RecommendationViewModelFactory
import com.cardwise.app.ui.scan.ScanScreen
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
    rewardRuleRepository: RewardRuleRepository? = null,
    recommendationRules: Map<Long, List<RewardRule>> = emptyMap()
) {
    CardWiseTheme {
        val context = LocalContext.current
        val paymentLauncher = remember(context.applicationContext) {
            AndroidUpiPaymentLauncher(context.applicationContext)
        }
        var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
        var walletScreen by rememberSaveable { mutableStateOf(WalletScreen.List) }
        var selectedCardId by rememberSaveable { mutableStateOf<Long?>(null) }
        var pendingPayment by remember { mutableStateOf<UpiPaymentRequest?>(null) }
        var showHandoffConfirmation by remember { mutableStateOf(false) }
        val destination = AppDestination.entries[selectedIndex]
        val application = context.applicationContext as CardWiseApplication
        val resolvedRepository = repository ?: application.container.cardRepository
        val resolvedRewardRuleRepository = rewardRuleRepository ?: application.container.rewardRuleRepository

        val walletViewModel: CardWalletViewModel = viewModel(
            factory = remember(resolvedRepository) { CardWalletViewModelFactory(resolvedRepository) }
        )
        val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
        val selectedCard = (walletState as? WalletUiState.Success)
            ?.cards?.firstOrNull { it.id == selectedCardId }

        val recommendationViewModel: RecommendationViewModel = viewModel(
            key = "recommendation",
            factory = remember(resolvedRepository, resolvedRewardRuleRepository, recommendationRules) {
                RecommendationViewModelFactory(
                    repository = resolvedRepository,
                    rewardRuleRepository = resolvedRewardRuleRepository,
                    rules = recommendationRules
                )
            }
        )

        fun requestPaymentHandoff(payment: UpiPaymentRequest) {
            pendingPayment = payment
            showHandoffConfirmation = true
        }

        Scaffold(
            bottomBar = {
                if (walletScreen == WalletScreen.List) {
                    NavigationBar {
                        AppDestination.entries.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = index == selectedIndex,
                                onClick = {
                                    selectedIndex = index
                                    if (item != AppDestination.Insights) pendingPayment = null
                                },
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
                when (currentDestination) {
                    AppDestination.Insights -> RecommendationScreen(
                        viewModel = recommendationViewModel,
                        payment = pendingPayment,
                        onContinueToPayment = pendingPayment?.let { payment ->
                            { requestPaymentHandoff(payment) }
                        }
                    )
                    AppDestination.Scan -> ScanScreen(
                        onPaymentDetected = { payment: UpiPaymentRequest ->
                            pendingPayment = payment
                            recommendationViewModel.prefillFromUpi(payment)
                            selectedIndex = AppDestination.entries.indexOf(AppDestination.Insights)
                        },
                        onPaymentHandoffRequested = ::requestPaymentHandoff
                    )
                    AppDestination.Wallet -> when (screen) {
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

        val payment = pendingPayment
        if (showHandoffConfirmation && payment != null) {
            PaymentHandoffDialog(
                payment = payment,
                launcher = paymentLauncher,
                onDismiss = { showHandoffConfirmation = false },
                onHandoffAttempted = { pendingPayment = null }
            )
        }
    }
}
