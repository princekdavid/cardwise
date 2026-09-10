package com.cardwise.app.navigation

enum class AppDestination(val label: String, val showInBottomBar: Boolean = true) {
    Cockpit("Cockpit"),
    Wallet("Cards"),
    Scan("Scan"),
    Reasoning("Reasoning", showInBottomBar = false),
    Offers("Offers"),
    Insights("Insights"),
    Recommendation("Best Way", showInBottomBar = false)
}
