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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    recommendationRules: Map<Long, List<RewardRule>> = emptyMap(),
    paymentLauncher: UpiPaymentLauncher? = null,
    initialPayment: UpiPaymentRequest? = null
) {
    CardWiseTheme {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val resolvedPaymentLauncher = paymentLauncher ?: remember(context.applicationContext) {
            AndroidUpiPaymentLauncher(context.applicationContext)
        }
        val snackbarHostState = remember { SnackbarHostState() }
        var selectedIndex by rememberSaveable {
            mutableIntStateOf(
                if (initialPayment != null) AppDestination.entries.indexOf(AppDestination.Insights) else 0
            )
        }
        var walletScreen by rememberSaveable { mutableStateOf(WalletScreen.List) }
        var selectedCardId by rememberSaveable { mutableStateOf<Long?>(null) }
        var pendingPayment by remember { mutableStateOf(initialPayment) }
        var showHandoffConfirmation by remember { mutableStateOf(false) }
        var awaitingPaymentReturn by remember { mutableStateOf(false) }
        var showPaymentReturnNotice by remember { mutableStateOf(false) }
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

        LaunchedEffect(initialPayment) {
            initialPayment?.let(recommendationViewModel::prefillFromUpi)
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && awaitingPaymentReturn) {
                    awaitingPaymentReturn = false
                    showHandoffConfirmation = false
                    showPaymentReturnNotice = true
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        LaunchedEffect(showPaymentReturnNotice) {
            if (!showPaymentReturnNotice) return@LaunchedEffect
            showPaymentReturnNotice = false
            snackbarHostState.showSnackbar(
                "Back from UPI app. Payment status is managed by your UPI app."
            )
        }

        fun requestPaymentHandoff(payment: UpiPaymentRequest) {
            if (!awaitingPaymentReturn) {
                pendingPayment = payment
                showHandoffConfirmation = true
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                icon = { Text(item.label.take(1), modifier = Modifier.semantics { contentDescription = item.label }) },
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
        if (showHandoffConfirmation && payment != null && !awaitingPaymentReturn) {
            PaymentHandoffDialog(
                payment = payment,
                launcher = resolvedPaymentLauncher,
                onDismiss = { showHandoffConfirmation = false },
                onHandoffCompleted = { result ->
                    when (result) {
                        UpiPaymentLaunchResult.Launched -> {
                            pendingPayment = null
                            awaitingPaymentReturn = true
                        }
                        UpiPaymentLaunchResult.NoUpiApp,
                        UpiPaymentLaunchResult.UnsafePayment -> {
                            showHandoffConfirmation = false
                        }
                    }
                }
            )
        }
    }
}