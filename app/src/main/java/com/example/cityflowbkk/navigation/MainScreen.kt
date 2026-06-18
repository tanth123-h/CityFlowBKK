package com.example.cityflowbkk.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.features.common.PlaceholderScreen
import com.example.cityflowbkk.features.home.HomeScreen
import com.example.cityflowbkk.features.route.RouteScreen
import com.example.cityflowbkk.features.route.RouteDetailsScreen
import com.example.cityflowbkk.features.route.RouteDetailsViewModel
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.navigation.CityFlowBottomBar

@Composable
fun MainScreen(
    requestedStartRoute: String? = null,
    routeRequestVersion: Int = 0,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(routeRequestVersion, requestedStartRoute) {
        val route = requestedStartRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

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
                RouteScreen(
                    onNavigateToDetails = { routeDetailsId ->
                        navController.navigate(Screen.RouteDetails.createRoute(routeDetailsId))
                    },
                )
            }
            composable(
                route = Screen.RouteDetails.route,
                arguments = listOf(
                    navArgument("routeDetailsId") { type = NavType.StringType },
                ),
            ) {
                // viewModel() here uses the NavBackStackEntry as the ViewModelStoreOwner and
                // automatically supplies the framework-managed SavedStateHandle via CreationExtras,
                // so RouteDetailsViewModel receives the correct SavedStateHandle populated by
                // the Navigation argument bundle — no manual construction needed.
                RouteDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = viewModel(),
                )
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
