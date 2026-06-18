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
import com.example.cityflowbkk.features.map.MapScreen
import com.example.cityflowbkk.features.tutorials.TutorialContentSection
import com.example.cityflowbkk.features.tutorials.TutorialsMenuScreen
import com.example.cityflowbkk.features.tutorials.TutorialsScreen
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
                    onQuickActionClick = { action ->
                        when (action.title) {
                            "Tutorial" -> navController.navigate(Screen.TutorialsMenu.route)
                            "Tour" -> navController.navigate(Screen.Route.route) // Temporary placeholder
                            "Plan Route" -> navController.navigate(Screen.Route.route)
                        }
                    },
                    onPlanRouteClick = {
                        navController.navigate(Screen.Route.route)
                    }
                )
            }
            composable(Screen.Map.route) {
                MapScreen()
            }
            composable(Screen.TutorialsMenu.route) {
                TutorialsMenuScreen(
                    onUsageClick = { navController.navigate(Screen.UsageTutorials.route) },
                    onFareClick = { navController.navigate(Screen.FareTutorials.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.UsageTutorials.route) {
                TutorialsScreen(
                    section = TutorialContentSection.Usage,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.FareTutorials.route) {
                TutorialsScreen(
                    section = TutorialContentSection.Fare,
                    onBackClick = { navController.popBackStack() }
                )
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
        }
    }
}
