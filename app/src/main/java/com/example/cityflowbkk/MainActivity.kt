package com.example.cityflowbkk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cityflowbkk.navigation.CityFlowNavGraph
import com.example.cityflowbkk.navigation.Screen
import com.example.cityflowbkk.ui.theme.CityFlowBKKTheme

class MainActivity : ComponentActivity() {
    private var requestedStartRoute by mutableStateOf<String?>(null)
    private var routeRequestVersion by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNavigationIntent(intent)
        setContent {
            CityFlowBKKTheme {
                CityFlowNavGraph(
                    requestedStartRoute = requestedStartRoute,
                    routeRequestVersion = routeRequestVersion,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_ROUTE, false) == true || intent?.action == ACTION_OPEN_ROUTE) {
            requestedStartRoute = Screen.Route.route
            routeRequestVersion += 1
        }
    }

    companion object {
        const val ACTION_OPEN_ROUTE = "com.example.cityflowbkk.action.OPEN_ROUTE"
        const val EXTRA_OPEN_ROUTE = "open_route"
    }
}
