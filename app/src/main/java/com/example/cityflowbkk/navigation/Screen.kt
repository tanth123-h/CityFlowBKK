package com.example.cityflowbkk.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Route : Screen("route")
    data object Map : Screen("map")
    data object Station : Screen("station")
    data object Profile : Screen("profile")
    data object Tutorial : Screen("tutorial")
    data object TutorialsMenu : Screen("tutorials_menu")
    data object UsageTutorials : Screen("usage_tutorials")
    data object FareTutorials : Screen("fare_tutorials")
    data object RouteDetails : Screen("route_details/{routeDetailsId}") {
        fun createRoute(routeDetailsId: String): String {
            return "route_details/$routeDetailsId"
        }
    }
}
