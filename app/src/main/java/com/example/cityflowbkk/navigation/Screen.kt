package com.example.cityflowbkk.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Route : Screen("route")
    data object Map : Screen("map")
    data object Station : Screen("station")
    data object Profile : Screen("profile")
    data object DiscoverBangkok : Screen("discover_bangkok")
    data object SavedPlaces : Screen("saved_places")
    // Route: saved_place_detail/{attractionId}
    data object SavedPlaceDetail : Screen("saved_place_detail/{attractionId}") {
        fun createRoute(attractionId: Int) = "saved_place_detail/$attractionId"
    }
}
