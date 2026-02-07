package com.upiscamshield.ui.screens

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object Result : Screen("result")
    object History : Screen("history")
    object Subscription : Screen("subscription")
    object Settings : Screen("settings")
}
