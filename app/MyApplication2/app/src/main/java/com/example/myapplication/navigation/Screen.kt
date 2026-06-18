package com.example.myapplication.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object UsageTutorials : Screen("tutorials_usage")
    data object FareTutorials : Screen("tutorials_fare")
}
