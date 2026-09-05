package com.cardwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class AppDestination(val label: String) {
    Wallet("Wallet"),
    Scan("Scan"),
    Insights("Insights")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                                    icon = { Text(item.label.take(1)) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = destination,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "destination_transition"
                        ) { currentDestination ->
                            Column(
                                modifier = Modifier.fillMaxSize(),
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
        }
    }
}

@androidx.compose.runtime.Composable
private fun CardWiseTheme(content: @androidx.compose.runtime.Composable () -> Unit) {
    MaterialTheme(content = content)
}
