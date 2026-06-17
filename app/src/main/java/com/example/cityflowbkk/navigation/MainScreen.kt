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
import com.example.cityflowbkk.features.common.PlaceholderScreen
import com.example.cityflowbkk.features.home.HomeScreen
import com.example.cityflowbkk.features.route.RouteScreen
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.navigation.CityFlowBottomBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            CityFlowBottomBar(
                selectedItem = routeToBottomNavItem(currentRoute),
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onPlanRouteClick = {
                        navController.navigate(Screen.Route.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Screen.Route.route) {
                RouteScreen()
            }
            composable(Screen.Station.route) {
                PlaceholderScreen(title = "Stations", icon = HomeIcon.Station)
            }
            composable(Screen.Profile.route) {
                PlaceholderScreen(title = "Profile", icon = HomeIcon.Profile)
            }
        }
    }
}
