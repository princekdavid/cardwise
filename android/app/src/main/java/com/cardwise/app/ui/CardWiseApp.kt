package com.cardwise.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.MaterialTheme
import com.cardwise.app.navigation.AppDestination
import com.cardwise.app.ui.theme.CardWiseMotion
import com.cardwise.app.ui.theme.CardWiseSpacing
import com.cardwise.app.ui.theme.CardWiseTheme

@Composable
fun CardWiseApp() {
    CardWiseTheme {
        var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
        val destination = AppDestination.entries[selectedIndex]

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(CardWiseSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CardWise",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = currentDestination.label,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
