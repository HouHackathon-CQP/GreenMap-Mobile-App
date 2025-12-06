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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.data.remote.dto.CurrentWeather
import com.houhackathon.greenmap_app.ui.components.GreenButton
import com.houhackathon.greenmap_app.ui.components.GreenGhostButton
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

                LocationPill(
                    text = "Định Công, Hà Nội",
                    onClick = {
                        if (hasPermissionState.value) {
                            onRequestLocation()
                        } else {
                            permissionLauncher.launch(locationPermissions)
                        }
                    }
                )

                if (viewState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(0.6f)
                    )
                }

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

@Composable
private fun LocationPill(
    text: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = Color.White
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Cập nhật theo vị trí của bạn",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherLarge(
    current: CurrentWeather?,
    onNavigateMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102840)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lúc này",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                    Text(
                        text = current?.temp?.let { "${it}°" } ?: "--°",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = current?.desc ?: "Đang tải...",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    Text(
                        text = current?.temp?.let { "Cảm giác như ${it}°" } ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2236)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Độ ẩm: ${current?.humidity ?: "--"}%",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Gió: ${current?.windSpeed ?: "--"} km/h",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    GreenGhostButton(
                        text = "Mở bản đồ",
                        onClick = onNavigateMap,
                        modifier = Modifier
                            .weight(0.6f, fill = false),
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastCardContainer(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2236)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
