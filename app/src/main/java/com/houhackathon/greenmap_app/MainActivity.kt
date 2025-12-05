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

package com.houhackathon.greenmap_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.houhackathon.greenmap_app.navigation.Navigator
import com.houhackathon.greenmap_app.navigation.featureASection
import com.houhackathon.greenmap_app.navigation.featureBSection
import com.houhackathon.greenmap_app.navigation.featureCSection
import com.houhackathon.greenmap_app.navigation.profileSection
import com.houhackathon.greenmap_app.navigation.rememberNavigationState
import com.houhackathon.greenmap_app.navigation.toEntries
import com.houhackathon.greenmap_app.ui.theme.setEdgeToEdgeConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable


@Serializable
data object Home : NavKey

@Serializable
data object RouteA1 : NavKey

@Serializable
data object Map : NavKey

@Serializable
data object RouteB1 : NavKey

@Serializable
data object Notification : NavKey

@Serializable
data object Profile : NavKey

@Serializable
data object RouteC1 : NavKey

private val TOP_LEVEL_ROUTES = mapOf<NavKey, NavBarItem>(
    Home to NavBarItem(icon = Icons.Default.Home, description = "Home"),
    Map to NavBarItem(icon = Icons.Default.LocationOn, description = "Map"),
    Notification to NavBarItem(icon = Icons.Default.Notifications, description = "Notifications"),
    Profile to NavBarItem(icon = Icons.Default.Person, description = "Profile"),
)

data class NavBarItem(
    val icon: ImageVector,
    val description: String
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val navigationState =
                rememberNavigationState(
                    startRoute = Home,
                    topLevelRoutes = TOP_LEVEL_ROUTES.keys
                )

            val navigator = remember {
                Navigator(
                    navigationState
                )
            }

            val entryProvider = entryProvider {
                featureASection(onNavigateMap = { navigator.navigate(Map) })
                featureBSection(onSubRouteClick = { navigator.navigate(RouteB1) })
                featureCSection(onSubRouteClick = { navigator.navigate(RouteC1) })
                profileSection(onLoginSuccess = { navigator.navigate(Home) })
            }

            Scaffold(bottomBar = {
                NavigationBar {
                    TOP_LEVEL_ROUTES.forEach { (key, value) ->
                        val isSelected = key == navigationState.topLevelRoute
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { navigator.navigate(key) },
                            icon = {
                                Icon(
                                    imageVector = value.icon,
                                    contentDescription = value.description
                                )
                            },
                            label = { Text(value.description) }
                        )
                    }
                }
            }) { paddingValues ->
                NavDisplay(
                    entries = navigationState.toEntries(entryProvider),
                    onBack = { navigator.goBack() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
