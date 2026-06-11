package com.example.cityflowbkk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cityflowbkk.features.home.HomeScreen
import com.example.cityflowbkk.ui.theme.CityFlowBKKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CityFlowBKKTheme {
                HomeScreen()
            }
        }
    }
}
