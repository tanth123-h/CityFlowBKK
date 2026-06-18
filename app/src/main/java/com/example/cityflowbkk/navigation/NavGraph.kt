package com.example.cityflowbkk.navigation

import androidx.compose.runtime.Composable

@Composable
fun CityFlowNavGraph(
    requestedStartRoute: String? = null,
    routeRequestVersion: Int = 0,
) {
    MainScreen(
        requestedStartRoute = requestedStartRoute,
        routeRequestVersion = routeRequestVersion,
    )
}
