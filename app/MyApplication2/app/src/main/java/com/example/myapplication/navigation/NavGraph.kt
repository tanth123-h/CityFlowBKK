package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.features.home.HomeScreen
import com.example.myapplication.features.tutorials.TutorialContentSection
import com.example.myapplication.features.tutorials.TutorialsScreen

@Composable
fun CityFlowNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onUsageClick = {
                    navController.navigate(Screen.UsageTutorials.route)
                },
                onFareClick = {
                    navController.navigate(Screen.FareTutorials.route)
                },
            )
        }
        composable(Screen.UsageTutorials.route) {
            TutorialsScreen(
                section = TutorialContentSection.Usage,
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(Screen.FareTutorials.route) {
            TutorialsScreen(
                section = TutorialContentSection.Fare,
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}
