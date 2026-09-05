package com.cardwise.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cardwise.app.ui.home.HomeScreen

private data class Destination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@Composable
fun CardWiseApp() {
    val navController = rememberNavController()
    val destinations = listOf(
        Destination("home", "Home") { Icon(Icons.Outlined.Home, contentDescription = null) },
        Destination("wallet", "Wallet") { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null) },
        Destination("scan", "Scan") { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null) },
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = destination.icon,
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") { HomeScreen() }
            composable("wallet") { PlaceholderScreen("Your cards") }
            composable("scan") { PlaceholderScreen("Scan & Pay") }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}
