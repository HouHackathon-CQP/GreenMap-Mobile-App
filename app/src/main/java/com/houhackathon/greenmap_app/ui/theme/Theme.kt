package com.houhackathon.greenmap_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Leaf300,
    onPrimary = Charcoal,
    secondary = Leaf200,
    onSecondary = Charcoal,
    tertiary = SkyGlow,
    background = Charcoal,
    onBackground = Color.White,
    surface = Color(0xFF24352A),
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Leaf500,
    onPrimary = Color.White,
    secondary = Leaf300,
    onSecondary = Leaf900,
    tertiary = SkyGlow,
    onTertiary = Leaf900,
    background = Sand,
    onBackground = Leaf900,
    surface = Color.White,
    onSurface = Leaf900,
    surfaceVariant = Leaf100,
    onSurfaceVariant = Leaf700
)

@Composable
fun GreenMapAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
