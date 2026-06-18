package com.example.cityflowbkk.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cityflowbkk.features.common.PlaceholderScreen
import com.example.cityflowbkk.features.home.HomeScreen
import com.example.cityflowbkk.features.map.MapScreen
import com.example.cityflowbkk.features.tour.DiscoverBangkokScreen
import com.example.cityflowbkk.features.tour.DiscoverViewModel
import com.example.cityflowbkk.features.tour.SavedPlaceDetailScreen
import com.example.cityflowbkk.features.tour.SavedPlacesScreen
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.navigation.CityFlowBottomBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val discoverViewModel: DiscoverViewModel = viewModel()

    // Hide bottom bar on these full-screen routes
    val noBottomBarRoutes = listOf(
        Screen.DiscoverBangkok.route,
        Screen.SavedPlaces.route,
        Screen.SavedPlaceDetail.route   // pattern with {attractionId}
    )
    val shouldShowBottomBar = noBottomBarRoutes.none { pattern ->
        currentRoute?.startsWith(pattern.substringBefore("{")) == true
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onTourClick = { navController.navigate(Screen.DiscoverBangkok.route) }
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
            composable(Screen.Map.route) {
                MapScreen()
            }
            composable(Screen.DiscoverBangkok.route) {
                DiscoverBangkokScreen(
                    viewModel = discoverViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSavedPlacesClick = { navController.navigate(Screen.SavedPlaces.route) }
                )
            }
            composable(Screen.SavedPlaces.route) {
                SavedPlacesScreen(
                    viewModel = discoverViewModel,
                    onBackClick = { navController.popBackStack() },
                    onPlaceClick = { attraction ->
                        navController.navigate(Screen.SavedPlaceDetail.createRoute(attraction.id))
                    }
                )
            }
            composable(
                route = Screen.SavedPlaceDetail.route,
                arguments = listOf(
                    navArgument("attractionId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val attractionId = backStackEntry.arguments?.getInt("attractionId")
                val attraction = attractionId?.let { discoverViewModel.getSavedPlaceById(it) }
                if (attraction != null) {
                    SavedPlaceDetailScreen(
                        attraction = attraction,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Place not found. Please go back and try again.")
                    }
                }
            }
        }
    }
}
