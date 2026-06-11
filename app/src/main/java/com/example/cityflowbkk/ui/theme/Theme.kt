package com.example.cityflowbkk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CityFlowBlueDark,
    secondary = CityFlowGreen,
    tertiary = CityFlowOrange,
    background = CityFlowSurfaceDark,
    surface = Color(0xFF171C21),
    surfaceVariant = Color(0xFF26313A),
    onPrimary = Color(0xFF08233F),
    onSecondary = Color(0xFF06210F),
    onTertiary = Color(0xFF302200),
    onBackground = CityFlowOnSurfaceDark,
    onSurface = CityFlowOnSurfaceDark,
    onSurfaceVariant = Color(0xFFC5D0DA)
)

private val LightColorScheme = lightColorScheme(
    primary = CityFlowBlue,
    secondary = CityFlowGreen,
    tertiary = CityFlowOrange,
    background = CityFlowBackground,
    surface = CityFlowWhite,
    surfaceVariant = Color(0xFFE8EEF5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF2B2100),
    onBackground = Color(0xFF17212B),
    onSurface = Color(0xFF17212B),
    onSurfaceVariant = Color(0xFF53616F),
)

@Composable
fun CityFlowBKKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
