package com.cardwise.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardwise.app.CardWiseApplication
import com.cardwise.app.data.catalog.CuratedCardCatalogProvider
import com.cardwise.app.data.catalog.InMemoryCardCatalogStore
import com.cardwise.app.data.offers.CuratedOfferProvider
import com.cardwise.app.data.offers.InMemoryOfferStore
import com.cardwise.app.domain.catalog.CardCatalogueEngine
import com.cardwise.app.domain.catalog.DefaultCardCatalogRepository
import com.cardwise.app.domain.model.PaymentHistoryEntry
import com.cardwise.app.domain.model.PaymentHistoryOutcome
import com.cardwise.app.domain.offers.DefaultOfferCatalogRepository
import com.cardwise.app.domain.offers.OfferEngine
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.domain.repository.PaymentHistoryRepository
import com.cardwise.app.domain.repository.RewardRuleRepository
import com.cardwise.app.domain.rewards.RewardRule
import com.cardwise.app.domain.scan.UpiPaymentRequest
import com.cardwise.app.navigation.AppDestination
import com.cardwise.app.ui.catalog.CardCatalogScreen
import com.cardwise.app.ui.catalog.CardCatalogViewModel
import com.cardwise.app.ui.catalog.CardCatalogViewModelFactory
import com.cardwise.app.ui.cockpit.CockpitScreen
import com.cardwise.app.ui.cockpit.CockpitViewModel
import com.cardwise.app.ui.insights.InsightsScreen
import com.cardwise.app.ui.insights.InsightsViewModel
import com.cardwise.app.ui.offers.OffersScreen
import com.cardwise.app.ui.offers.OffersViewModel
import com.cardwise.app.ui.reasoning.ReasoningScreen
import com.cardwise.app.ui.recommendation.RecommendationScreen
import com.cardwise.app.ui.recommendation.RecommendationUiState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private enum class WalletScreen { List, Add, Detail, Edit }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardWiseApp(
    repository: CardRepository? = null,
    rewardRuleRepository: RewardRuleRepository? = null,
    paymentHistoryRepository: PaymentHistoryRepository? = null,
    recommendationRules: Map<Long, List<RewardRule>> = emptyMap(),
    paymentLauncher: UpiPaymentLauncher? = null,
    initialPayment: UpiPaymentRequest? = null
) {
    var darkTheme by rememberSaveable { mutableStateOf(true) }
    CardWiseTheme(darkTheme = darkTheme) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val coroutineScope = rememberCoroutineScope()
        val application = context.applicationContext as CardWiseApplication
        val resolvedRepository = repository ?: application.container.cardRepository
        val resolvedRewardRuleRepository = rewardRuleRepository ?: if (recommendationRules.isEmpty()) application.container.rewardRuleRepository else null
        val resolvedPaymentHistoryRepository = paymentHistoryRepository ?: application.container.paymentHistoryRepository
        val resolvedPaymentLauncher = paymentLauncher ?: remember(context.applicationContext) { AndroidUpiPaymentLauncher(context.applicationContext) }
        val snackbarHostState = remember { SnackbarHostState() }
        val catalogRepository = remember { DefaultCardCatalogRepository(CardCatalogueEngine(providers = listOf(CuratedCardCatalogProvider()), store = InMemoryCardCatalogStore())) }
        val offerRepository = remember { DefaultOfferCatalogRepository(OfferEngine(providers = listOf(CuratedOfferProvider()), store = InMemoryOfferStore())) }

        var destination by rememberSaveable { mutableStateOf(if (initialPayment != null) AppDestination.Reasoning else AppDestination.Cockpit) }
        var walletScreen by rememberSaveable { mutableStateOf(WalletScreen.List) }
        var selectedCardId by rememberSaveable { mutableStateOf<Long?>(null) }
        var pendingPayment by remember { mutableStateOf(initialPayment) }
        var showHandoffConfirmation by remember { mutableStateOf(false) }
        var awaitingPaymentReturn by remember { mutableStateOf(false) }
        var showPaymentReturnNotice by remember { mutableStateOf(false) }

        val cockpitViewModel: CockpitViewModel = viewModel(key = "cockpit")
        val walletViewModel: CardWalletViewModel = viewModel(factory = remember(resolvedRepository) { CardWalletViewModelFactory(resolvedRepository) })
        val catalogViewModel: CardCatalogViewModel = viewModel(key = "catalog", factory = remember(catalogRepository) { CardCatalogViewModelFactory(catalogRepository) })
        val offersViewModel: OffersViewModel = viewModel(key = "offers", factory = remember(offerRepository) { OffersViewModel.factory(offerRepository) })
        val insightsViewModel: InsightsViewModel = viewModel(key = "insights", factory = remember(resolvedPaymentHistoryRepository) { InsightsViewModel.factory(resolvedPaymentHistoryRepository) })
        val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
        val selectedCard = (walletState as? WalletUiState.Success)?.cards?.firstOrNull { it.id == selectedCardId }
        val recommendationViewModel: RecommendationViewModel = viewModel(key = "recommendation", factory = remember(resolvedRepository, resolvedRewardRuleRepository, recommendationRules) { RecommendationViewModelFactory(resolvedRepository, resolvedRewardRuleRepository, recommendationRules) })

        LaunchedEffect(initialPayment) { initialPayment?.let(recommendationViewModel::prefillFromUpi) }
        LaunchedEffect(showPaymentReturnNotice) {
            if (showPaymentReturnNotice) {
                snackbarHostState.showSnackbar("Back from UPI app. Payment status is managed by the UPI app.")
                showPaymentReturnNotice = false
            }
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

        fun requestHandoff(payment: UpiPaymentRequest) {
            pendingPayment = payment
            showHandoffConfirmation = true
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { if (destination.showInBottomBar && walletScreen == WalletScreen.List) CardWiseNavigationBar(destination = destination, onDestinationSelected = { destination = it }) }
        ) { paddingValues ->
            AnimatedContent(
                targetState = destination to walletScreen,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                transitionSpec = { fadeIn(tween(CardWiseMotion.screenTransitionMillis)) togetherWith fadeOut(tween(CardWiseMotion.screenTransitionMillis)) },
                label = "cardwise_screen_transition"
            ) { (currentDestination, currentWalletScreen) ->
                when (currentDestination) {
                    AppDestination.Cockpit -> CockpitScreen(
                        cards = (walletState as? WalletUiState.Success)?.cards.orEmpty(), viewModel = cockpitViewModel,
                        onScan = { destination = AppDestination.Scan },
                        onCalculate = { amount, category -> recommendationViewModel.setAmount(amount); recommendationViewModel.setCategory(category); pendingPayment = null; destination = AppDestination.Recommendation },
                        onOpenCards = { destination = AppDestination.Wallet }, onToggleTheme = { darkTheme = !darkTheme }, darkTheme = darkTheme
                    )
                    AppDestination.Wallet -> when (currentWalletScreen) {
                        WalletScreen.List -> CardWalletScreen(viewModel = walletViewModel, onAddCard = { walletScreen = WalletScreen.Add }, onOpenCard = { card -> selectedCardId = card.id; walletScreen = WalletScreen.Detail })
                        WalletScreen.Add -> CardCatalogScreen(viewModel = catalogViewModel, walletViewModel = walletViewModel, onBack = { walletScreen = WalletScreen.List })
                        WalletScreen.Detail -> CardWalletScreen(viewModel = walletViewModel, onAddCard = { walletScreen = WalletScreen.Add }, onOpenCard = { card -> selectedCardId = card.id })
                        WalletScreen.Edit -> if (selectedCard != null) CardFormScreen(viewModel = walletViewModel, existingCard = selectedCard, onDone = { walletScreen = WalletScreen.Detail }) else Text("Card not found", modifier = Modifier.padding(CardWiseSpacing.lg))
                    }
                    AppDestination.Scan -> ScanScreen(onPaymentDetected = { payment -> pendingPayment = payment; recommendationViewModel.prefillFromUpi(payment); destination = AppDestination.Reasoning }, onPaymentHandoffRequested = ::requestHandoff)
                    AppDestination.Reasoning -> ReasoningScreen(viewModel = recommendationViewModel, payment = pendingPayment, onComplete = { destination = AppDestination.Recommendation })
                    AppDestination.Offers -> OffersScreen(viewModel = offersViewModel)
                    AppDestination.Insights -> InsightsScreen(viewModel = insightsViewModel)
                    AppDestination.Recommendation -> RecommendationScreen(
                        viewModel = recommendationViewModel,
                        payment = pendingPayment,
                        onContinueToPayment = pendingPayment?.let { { requestHandoff(it) } },
                        onRescan = pendingPayment?.let { { destination = AppDestination.Scan } },
                        onAdjustDetails = { destination = AppDestination.Cockpit }
                    )
                }
            }
        }

        if (destination == AppDestination.Wallet && walletScreen == WalletScreen.Detail && selectedCard != null) {
            ModalBottomSheet(onDismissRequest = { walletScreen = WalletScreen.List; selectedCardId = null }) {
                Column(Modifier.padding(bottom = CardWiseSpacing.lg)) {
                    CardDetailScreen(card = selectedCard, onEdit = { walletScreen = WalletScreen.Edit }, onDelete = { walletViewModel.deleteCard(selectedCard.id); selectedCardId = null; walletScreen = WalletScreen.List }, onToggleActive = { walletViewModel.updateCard(selectedCard.copy(isActive = !selectedCard.isActive)) }, onBack = { walletScreen = WalletScreen.List; selectedCardId = null })
                }
            }
        }

        val payment = pendingPayment
        if (showHandoffConfirmation && payment != null && !awaitingPaymentReturn) {
            PaymentHandoffDialog(payment = payment, launcher = resolvedPaymentLauncher, onDismiss = { showHandoffConfirmation = false }, onHandoffCompleted = { result ->
                when (result) {
                    UpiPaymentLaunchResult.Launched -> {
                        val ready = recommendationViewModel.uiState.value as? RecommendationUiState.Ready
                        val amount = ready?.input?.amount?.toDoubleOrNull()
                        val category = ready?.input?.category?.trim().orEmpty().ifBlank { "Other" }
                        val recommendation = ready?.recommendations?.firstOrNull()
                        if (amount != null && recommendation != null) {
                            coroutineScope.recordHistory(resolvedPaymentHistoryRepository, amount, category, recommendation.card.id, recommendation.reward.estimatedReward)
                        }
                        pendingPayment = null
                        awaitingPaymentReturn = true
                    }
                    UpiPaymentLaunchResult.NoUpiApp, UpiPaymentLaunchResult.UnsafePayment -> showHandoffConfirmation = false
                }
            })
        }
    }
}

private fun CoroutineScope.recordHistory(
    repository: PaymentHistoryRepository,
    amount: Double,
    category: String,
    cardId: Long,
    rewardAmount: Double
) {
    launch {
        repository.record(
            PaymentHistoryEntry(
                id = 0L,
                occurredAtEpochMillis = System.currentTimeMillis(),
                amount = amount,
                category = category,
                cardId = cardId,
                rewardAmount = rewardAmount,
                outcome = PaymentHistoryOutcome.HANDOFF_STARTED
            )
        )
    }
}
