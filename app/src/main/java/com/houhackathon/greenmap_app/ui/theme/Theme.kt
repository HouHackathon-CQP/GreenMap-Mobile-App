/*
 * Copyright 2025 HouHackathon-CQP
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
