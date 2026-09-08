package com.cardwise.app.navigation

enum class AppDestination(val label: String, val showInBottomBar: Boolean = true) {
    Cockpit("Cockpit"),
    Wallet("Cards"),
    Scan("Scan"),
    Offers("Offers"),
    Recommendation("Best Way", showInBottomBar = false)
}
