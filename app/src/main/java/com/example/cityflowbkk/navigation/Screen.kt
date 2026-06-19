package com.example.cityflowbkk.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Route : Screen("route?destination={destination}") {
        /** Navigate to Route screen with an optional pre-filled destination. */
        fun createRoute(destination: String = "") =
            if (destination.isBlank()) "route?destination="
            else "route?destination=${java.net.URLEncoder.encode(destination, "UTF-8")}"
    }
    data object Map : Screen("map")
    data object Station : Screen("station")
    data object Profile : Screen("profile")
    data object DiscoverBangkok : Screen("discover_bangkok")
    data object SavedPlaces : Screen("saved_places")

    data object SavedPlaceDetail : Screen("saved_place_detail/{attractionId}") {
        fun createRoute(attractionId: Int) = "saved_place_detail/$attractionId"
    }

    data object RouteDetails : Screen("route_details/{routeDetailsId}") {
        fun createRoute(routeDetailsId: String): String {
            return "route_details/$routeDetailsId"
        }
    }
}
