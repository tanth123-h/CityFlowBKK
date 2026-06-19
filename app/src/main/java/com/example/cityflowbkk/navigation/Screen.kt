package com.example.cityflowbkk.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Route : Screen("route")
    data object Map : Screen("map")
    data object Station : Screen("station")
    data object Profile : Screen("profile")
    data object BtsMap : Screen("bts_map")
    data object StationMapping : Screen("station_mapping")
}
