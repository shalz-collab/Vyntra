package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Dark Corporate Theme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), // Light blue
    primaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF93C5FD),
    tertiary = Color(0xFF2DD4BF),
    background = Color(0xFF0F172A), // Slate 900
    surface = Color(0xFF1E293B), // Slate 800
    onPrimary = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

// Light Clean Theme
private val LightColorScheme = lightColorScheme(
    primary = VyntraPrimaryDark,
    primaryContainer = VyntraPrimaryLevel,
    secondary = VyntraSecondary,
    tertiary = VyntraTealAccent,
    background = VyntraBgLight,
    surface = VyntraSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = VyntraNavy,
    onSurface = VyntraNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false by default for predictive dashboards 
    // to strictly enforce our blue/navy corporate branding experience.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
