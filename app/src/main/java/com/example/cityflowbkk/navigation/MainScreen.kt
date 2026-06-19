package com.example.cityflowbkk.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cityflowbkk.features.btsmap.BTSMapScreen
import com.example.cityflowbkk.features.common.PlaceholderScreen
import com.example.cityflowbkk.features.home.HomeScreen
import com.example.cityflowbkk.features.map.MapScreen
import com.example.cityflowbkk.features.stationmapping.BTSMapScreen as MappingToolScreen
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.navigation.CityFlowBottomBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on BtsMap screen
    val showBottomBar = currentRoute != Screen.BtsMap.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CityFlowBottomBar(
                    selectedItem = routeToBottomNavItem(currentRoute),
                    onItemClick = { item ->
                        val route = if (item == BottomNavItem.Map) Screen.BtsMap.route else item.route
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToBtsMap = {
                        navController.navigate(Screen.BtsMap.route)
                    },
                    onNavigateToMap = {
                        navController.navigate(Screen.Map.route)
                    },
                    onPlanRouteClick = {
                        navController.navigate(Screen.Map.route)
                    },
                    onNavigateToStationMapping = {
                        navController.navigate(Screen.StationMapping.route)
                    }
                )
            }
            composable(Screen.Map.route) {
                MapScreen()
            }
            composable(Screen.Route.route) {
                PlaceholderScreen(title = "Routes", icon = HomeIcon.Route)
            }
            composable(Screen.Station.route) {
                PlaceholderScreen(title = "Stations", icon = HomeIcon.Station)
            }
            composable(Screen.Profile.route) {
                PlaceholderScreen(title = "Profile", icon = HomeIcon.Profile)
            }
            composable(Screen.BtsMap.route) {
                BTSMapScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.StationMapping.route) {
                MappingToolScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
