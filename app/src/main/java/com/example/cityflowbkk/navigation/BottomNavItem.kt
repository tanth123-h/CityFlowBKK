package com.example.cityflowbkk.navigation

import com.example.cityflowbkk.ui.icons.HomeIcon

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: HomeIcon,
) {
    Home(Screen.Home.route, "Home", HomeIcon.Home),
    Route(Screen.Route.route, "Route", HomeIcon.Route),
    Station(Screen.Station.route, "Station", HomeIcon.Station),
    Profile(Screen.Profile.route, "Profile", HomeIcon.Profile),
}

fun routeToBottomNavItem(route: String?): BottomNavItem {
    return BottomNavItem.entries.firstOrNull { it.route == route } ?: BottomNavItem.Home
}
