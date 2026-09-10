package com.cardwise.app.navigation

enum class AppDestination(val label: String, val showInBottomBar: Boolean = true) {
    Onboarding("Onboarding", showInBottomBar = false),
    Cockpit("Cockpit"),
    Wallet("Cards"),
    Scan("Scan"),
    Reasoning("Reasoning", showInBottomBar = false),
    Offers("Offers"),
    Insights("Insights"),
    Vault("Privacy"),
    Recommendation("Best Way", showInBottomBar = false)
}
