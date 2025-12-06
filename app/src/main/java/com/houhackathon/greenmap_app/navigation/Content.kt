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

package com.houhackathon.greenmap_app.navigation

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.houhackathon.greenmap_app.Home
import com.houhackathon.greenmap_app.Map
import com.houhackathon.greenmap_app.Profile
import com.houhackathon.greenmap_app.ui.auth.LoginEvent
import com.houhackathon.greenmap_app.ui.auth.LoginIntent
import com.houhackathon.greenmap_app.ui.auth.LoginScreen
import com.houhackathon.greenmap_app.ui.auth.LoginViewModel
import com.houhackathon.greenmap_app.ui.auth.ProfileScreen
import com.houhackathon.greenmap_app.ui.home.HomeEvent
import com.houhackathon.greenmap_app.ui.home.HomeIntent
import com.houhackathon.greenmap_app.ui.home.HomeScreen
import com.houhackathon.greenmap_app.ui.home.HomeViewModel
import com.houhackathon.greenmap_app.ui.map.MapScreen


fun EntryProviderScope<NavKey>.featureASection(
    onNavigateMap: () -> Unit,
) {
    entry<Home> {
        val viewModel: HomeViewModel = hiltViewModel()
        val viewState by viewModel.viewState.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            viewModel.singleEvent.collect { event ->
                when (event) {
                    is HomeEvent.ShowToast -> {
                        android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                    }

                    HomeEvent.NavigateMap -> onNavigateMap()
                }
            }
        }

        HomeScreen(
            viewState = viewState,
            onRequestLocation = { viewModel.processIntent(HomeIntent.UseCurrentLocation) },
            onRefreshForecast = { viewModel.processIntent(HomeIntent.RefreshForecast) },
            onNavigateMap = { viewModel.processIntent(HomeIntent.NavigateToMap) }
        )

    }
}

fun EntryProviderScope<NavKey>.featureBSection(
    onSubRouteClick: () -> Unit,
) {
    entry<Map> {
        MapScreen()
    }

}

fun EntryProviderScope<NavKey>.featureCSection(
    onSubRouteClick: () -> Unit,
) {
    entry<com.houhackathon.greenmap_app.Notification> {

    }
}

fun EntryProviderScope<NavKey>.profileSection(
    onLoginSuccess: () -> Unit,
) {
    entry<Profile> {
        val viewModel: LoginViewModel = hiltViewModel()
        val viewState by viewModel.viewState.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            viewModel.singleEvent.collect { event ->
                when (event) {
                    is LoginEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    LoginEvent.LoginSuccess -> onLoginSuccess()
                }
            }
        }

        if (!viewState.isLoggedIn) {
            LoginScreen(
                onLogin = { u, p ->
                    viewModel.processIntent(LoginIntent.UpdateEmail(u))
                    viewModel.processIntent(LoginIntent.UpdatePassword(p))
                    viewModel.processIntent(LoginIntent.Submit)
                },
                username = viewState.email,
                password = viewState.password
            )
        } else {
            ProfileScreen(
                fullName = viewState.fullName ?: viewState.email,
                email = viewState.email,
                onLogout = { viewModel.processIntent(LoginIntent.Logout) }
            )
        }
    }
}
