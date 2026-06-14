package com.example.cityflowbkk.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
}
