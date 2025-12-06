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

package com.houhackathon.greenmap_app.ui.home

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.ui.components.GreenButton
import com.houhackathon.greenmap_app.ui.components.GreenGhostButton
import com.houhackathon.greenmap_app.ui.home.components.CurrentWeatherCard
import com.houhackathon.greenmap_app.ui.home.components.DailyForecastList
import com.houhackathon.greenmap_app.ui.home.components.HeaderSection
import com.houhackathon.greenmap_app.ui.home.components.HourlyForecastRow

@Composable
fun HomeScreen(
    viewState: HomeViewState,
    onRequestLocation: () -> Unit,
    onRefreshForecast: () -> Unit,
    onNavigateMap: () -> Unit,
) {
    val context = LocalContext.current
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    val hasPermissionState = remember { mutableStateOf(hasLocationPermission(context)) }
    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.any { it.value }
        hasPermissionState.value = granted
        if (granted) onRequestLocation()
    }

    LaunchedEffect(hasPermissionState.value) {
        if (hasPermissionState.value) {
            onRequestLocation()
        } else {
            permissionLauncher.launch(locationPermissions)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F9D58), Color(0xFF1EB980))
                )
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection(
                    actionIcon = Icons.Outlined.LocationOn,
                    onActionClick = {
                        if (hasPermissionState.value) {
                            onRequestLocation()
                        } else {
                            permissionLauncher.launch(locationPermissions)
                        }
                    }
                )

                if (viewState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                CurrentWeatherCard(
                    current = viewState.forecast?.data?.current,
                    source = viewState.forecast?.source
                )

                HourlyForecastRow(
                    items = viewState.forecast?.data?.hourly ?: emptyList()
                )

                DailyForecastList(
                    items = viewState.forecast?.data?.daily ?: emptyList()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GreenGhostButton(
                        text = stringResource(id = R.string.btn_open_map),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateMap
                    )
                    GreenButton(
                        text = stringResource(id = R.string.btn_refresh),
                        modifier = Modifier.weight(1f),
                        onClick = onRefreshForecast
                    )
                }

                if (!viewState.error.isNullOrBlank()) {
                    Text(
                        text = viewState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    return fine == android.content.pm.PackageManager.PERMISSION_GRANTED ||
        coarse == android.content.pm.PackageManager.PERMISSION_GRANTED
}
