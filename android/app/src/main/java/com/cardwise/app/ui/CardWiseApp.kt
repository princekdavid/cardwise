package com.cardwise.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cardwise.app.CardWiseApplication
import com.cardwise.app.navigation.AppDestination
import com.cardwise.app.domain.repository.CardRepository
import com.cardwise.app.ui.theme.CardWiseMotion
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.CardWiseTheme
import com.cardwise.app.ui.wallet.CardWalletScreen
import com.cardwise.app.ui.wallet.CardWalletViewModel
import com.cardwise.app.ui.wallet.CardWalletViewModelFactory

@Composable
fun CardWiseApp(repository: CardRepository? = null) {
    CardWiseTheme {
        var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
        val destination = AppDestination.entries[selectedIndex]
        val resolvedRepository = repository ?: (LocalContext.current.applicationContext as CardWiseApplication)
            .container.cardRepository
        val walletViewModel: CardWalletViewModel = viewModel(
            factory = CardWalletViewModelFactory(resolvedRepository)
        )

        Scaffold(
            bottomBar = {
                NavigationBar {
                    AppDestination.entries.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            icon = {
                                Text(
                                    text = item.label.take(1),
                                    modifier = Modifier.semantics {
                                        contentDescription = "${item.label} tab"
                                    }
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = destination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                transitionSpec = {
                    fadeIn(tween(CardWiseMotion.screenTransitionMillis)) togetherWith
                        fadeOut(tween(CardWiseMotion.screenTransitionMillis))
                },
                label = "destination_transition"
            ) { currentDestination ->
                when (currentDestination) {
                    AppDestination.Wallet -> CardWalletScreen(
                        viewModel = walletViewModel,
                        onAddCard = { }
                    )
                    else -> Text(
                        text = currentDestination.label,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(CardWiseSpacing.lg)
                    )
                }
            }
        }
    }
}
