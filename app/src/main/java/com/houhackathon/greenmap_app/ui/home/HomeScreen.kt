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
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.houhackathon.greenmap_app.domain.model.AiProvider
import com.houhackathon.greenmap_app.ui.home.components.AiInsightCard
import com.houhackathon.greenmap_app.ui.home.components.CurrentWeatherLarge
import com.houhackathon.greenmap_app.ui.home.components.DailyForecastList
import com.houhackathon.greenmap_app.ui.home.components.ForecastCardContainer
import com.houhackathon.greenmap_app.ui.home.components.HeaderSection
import com.houhackathon.greenmap_app.ui.home.components.HourlyForecastRow
import com.houhackathon.greenmap_app.ui.home.components.LocationPill
import com.houhackathon.greenmap_app.ui.home.components.QuickActionsRow
import java.util.Locale

@Composable
fun HomeScreen(
    viewState: HomeViewState,
    onRequestLocation: () -> Unit,
    onRefreshForecast: () -> Unit,
    onAnalyzeWithAi: (Boolean) -> Unit,
    onSelectAiProvider: (AiProvider) -> Unit,
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
                    colors = listOf(Color(0xFF0B1B2B), Color(0xFF0D2437), Color(0xFF0E2E45))
                )
            )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HeaderSection()

                val formattedLat =
                    viewState.lat.toDoubleOrNull()?.let { String.format(Locale.US, "%.4f", it) }
                        ?: viewState.lat
                val formattedLon =
                    viewState.lon.toDoubleOrNull()?.let { String.format(Locale.US, "%.4f", it) }
                        ?: viewState.lon
                val locationLabel = viewState.locationName?.takeIf { it.isNotBlank() }
                    ?: if (viewState.lat.isNotBlank() && viewState.lon.isNotBlank()) {
                        "Vĩ độ $formattedLat | Kinh độ $formattedLon"
                    } else {
                        "Định Công, Hà Nội"
                    }

                LocationPill(
                    text = locationLabel,
                    onClick = {
                        if (hasPermissionState.value) {
                            onRequestLocation()
                        } else {
                            permissionLauncher.launch(locationPermissions)
                        }
                    }
                )

                QuickActionsRow(
                    onRefreshForecast = onRefreshForecast,
                    onAnalyzeAi = { onAnalyzeWithAi(false) }
                )

                AiInsightCard(
                    provider = viewState.selectedAiProvider,
                    model = viewState.aiModel,
                    analysis = viewState.aiAnalysis,
                    isLoading = viewState.isAiLoading,
                    error = viewState.aiError,
                    onProviderSelected = onSelectAiProvider,
                    onAnalyzeClick = { onAnalyzeWithAi(true) }
                )

                CurrentWeatherLarge(
                    current = viewState.forecast?.data?.current,
                    onNavigateMap = onNavigateMap
                )

                ForecastCardContainer {
                    HourlyForecastRow(
                        items = viewState.forecast?.data?.hourly ?: emptyList()
                    )
                }

                ForecastCardContainer {
                    DailyForecastList(
                        items = viewState.forecast?.data?.daily ?: emptyList()
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
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    return fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED
}
