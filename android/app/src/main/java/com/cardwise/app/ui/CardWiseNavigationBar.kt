package com.cardwise.app.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cardwise.app.navigation.AppDestination

/** Shared bottom navigation presentation for the primary CardWise destinations. */
@Composable
fun CardWiseNavigationBar(
    destination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        listOf(
            AppDestination.Cockpit,
            AppDestination.Wallet,
            AppDestination.Scan,
            AppDestination.Offers,
            AppDestination.Insights
        ).forEach { item ->
            NavigationBarItem(
                selected = destination == item,
                onClick = { onDestinationSelected(item) },
                icon = { Text(item.label.take(1)) },
                label = { Text(item.label) },
                modifier = Modifier
                    .testTag("nav_${item.name.lowercase()}")
                    .semantics { contentDescription = item.label }
            )
        }
    }
}
