package com.example.cityflowbkk.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
tindersuper
import com.example.cityflowbkk.features.common.PlaceholderScreen
import com.example.cityflowbkk.features.home.HomeScreen
import com.example.cityflowbkk.features.map.MapScreen
tutorials
import com.example.cityflowbkk.features.tutorials.TutorialContentSection
import com.example.cityflowbkk.features.tutorials.TutorialsMenuScreen
import com.example.cityflowbkk.features.tutorials.TutorialsScreen

import com.example.cityflowbkk.features.tour.DiscoverBangkokScreen
import com.example.cityflowbkk.features.tour.DiscoverViewModel
import com.example.cityflowbkk.features.tour.SavedPlaceDetailScreen
import com.example.cityflowbkk.features.tour.SavedPlacesScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.features.common.PlaceholderScreen
import com.example.cityflowbkk.features.home.HomeScreen
import com.example.cityflowbkk.features.route.RouteScreen
import com.example.cityflowbkk.features.route.RouteDetailsScreen
import com.example.cityflowbkk.features.route.RouteDetailsViewModel
 master
 master
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

 tindersuper
    val discoverViewModel: DiscoverViewModel = viewModel()

    // Hide bottom bar on these full-screen routes
    val noBottomBarRoutes = listOf(
        Screen.DiscoverBangkok.route,
        Screen.SavedPlaces.route,
        Screen.SavedPlaceDetail.route   // pattern with {attractionId}
    )
    val shouldShowBottomBar = noBottomBarRoutes.none { pattern ->
        currentRoute?.startsWith(pattern.substringBefore("{")) == true

    LaunchedEffect(routeRequestVersion, requestedStartRoute) {
        val route = requestedStartRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
 master
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
tutorials
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
tindersuper
                    onTourClick = { navController.navigate(Screen.DiscoverBangkok.route) }

                    onPlanRouteClick = {
                        navController.navigate(Screen.Route.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
 master
master
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
