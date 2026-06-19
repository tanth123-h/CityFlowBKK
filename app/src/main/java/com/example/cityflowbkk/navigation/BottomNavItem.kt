package com.example.cityflowbkk.navigation

import com.example.cityflowbkk.ui.icons.HomeIcon

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: HomeIcon,
) {
    Home(Screen.Home.route, "Home", HomeIcon.Home),
    Route(Screen.Route.route, "Route", HomeIcon.Route),
    Map(Screen.Map.route, "Map", HomeIcon.Map),
    Station(Screen.Station.route, "Station", HomeIcon.Station),
    Profile(Screen.Profile.route, "Profile", HomeIcon.Profile),
}

fun routeToBottomNavItem(route: String?): BottomNavItem {
    if (route == Screen.BtsMap.route) return BottomNavItem.Map
    return BottomNavItem.entries.firstOrNull { it.route == route } ?: BottomNavItem.Home
}
