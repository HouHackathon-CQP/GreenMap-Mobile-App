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

package com.houhackathon.greenmap_app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.houhackathon.greenmap_app.BuildConfig
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.ui.theme.Leaf700
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import toMapLibreIcon

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val viewModel: MapViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current
    val mapView = remember { MapViewHolder.getOrCreate(context) }
    var isMapReady by remember { mutableStateOf(MapViewHolder.isInitialized()) }
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val weatherMarkers = remember { mutableStateListOf<Marker>() }
    val aqiMarkers = remember { mutableStateListOf<Marker>() }
    val aqiIcon = context.toMapLibreIcon(R.drawable.ic_aqi, Leaf700)

    MapEventHandler(viewModel)
    LocationPermissionEffect(
        hasLocationPermission = hasLocationPermission,
        mapView = mapView,
        onPermissionChanged = { hasLocationPermission = it }
    )
    MapInitializer(
        mapView = mapView,
        hasLocationPermission = hasLocationPermission,
        onMapReady = { mapLibreMap = it },
        onMapPrepared = { isMapReady = true }
    )
    WeatherMarkersEffect(
        weatherStations = viewState.weatherStations,
        mapLibreMap = mapLibreMap,
        markers = weatherMarkers
    )
    AqiMarkersEffect(
        aqiStations = viewState.aqiStations,
        mapLibreMap = mapLibreMap,
        markers = aqiMarkers,
        aqiIcon = aqiIcon
    )
    MapLifecycleHandler(mapView)
    MapContent(
        modifier = modifier,
        mapView = mapView,
        isMapReady = isMapReady,
        errorMessage = viewState.error
    )
}

@Composable
private fun MapContent(
    modifier: Modifier,
    mapView: MapView,
    isMapReady: Boolean,
    errorMessage: String?
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { /* giữ nguyên để tránh recreate khi recompose */ }
        )

        if (!isMapReady) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun MapEventHandler(viewModel: MapViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.singleEvent.collect { event ->
            when (event) {
                is MapEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}

@Composable
private fun LocationPermissionEffect(
    hasLocationPermission: Boolean,
    mapView: MapView,
    onPermissionChanged: (Boolean) -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onPermissionChanged(granted)
        if (granted && MapViewHolder.isInitialized()) {
            mapView.getMapAsync { enableMyLocation(it, mapView) }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

@Composable
private fun MapInitializer(
    mapView: MapView,
    hasLocationPermission: Boolean,
    onMapReady: (MapLibreMap) -> Unit,
    onMapPrepared: () -> Unit
) {
    LaunchedEffect(mapView, hasLocationPermission) {
        if (!MapViewHolder.isInitialized()) {
            mapView.getMapAsync { map ->
                setupMap(map, mapView, hasLocationPermission) {
                    onMapReady(map)
                }
                MapViewHolder.markInitialized()
                onMapPrepared()
            }
        } else {
            if (hasLocationPermission) {
                mapView.getMapAsync { enableMyLocation(it, mapView) }
            }
            mapView.getMapAsync { onMapReady(it) }
            onMapPrepared()
        }
    }
}

@Composable
private fun WeatherMarkersEffect(
    weatherStations: List<WeatherStationMarker>,
    mapLibreMap: MapLibreMap?,
    markers: MutableList<Marker>
) {
    LaunchedEffect(weatherStations, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        markers.forEach { map.removeAnnotation(it) }
        markers.clear()
        weatherStations.forEach { station ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(station.lat, station.lon))
                    .title(station.name)
                    .snippet(
                        listOfNotNull(
                            station.weatherType,
                            station.temperature?.let { "${"%.1f".format(it)}°C" }
                        ).joinToString(" • ")
                    )
            )
            markers.add(marker)
        }
    }
}

@Composable
private fun AqiMarkersEffect(
    aqiStations: List<AqiStationMarker>,
    mapLibreMap: MapLibreMap?,
    markers: MutableList<Marker>,
    aqiIcon: Icon?
) {
    LaunchedEffect(aqiStations, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        markers.forEach { map.removeAnnotation(it) }
        markers.clear()
        aqiStations.forEach { station ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(station.lat, station.lon))
                    .title(station.name)
                    .snippet(buildAqiSnippet(station))
                    .icon(aqiIcon)
            )
            markers.add(marker)
        }
    }
}

@Composable
private fun MapLifecycleHandler(mapView: MapView) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, mapView) {
        mapView.onStart()
        mapView.onResume()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                // Không destroy ở đây để giữ MapView khi đổi tab
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
        }
    }
}

private fun buildAqiSnippet(station: AqiStationMarker): String {
    val parts = listOfNotNull(
        station.aqi?.let { "AQI: $it" },
        station.aqiCategory?.label?.let { "Mức độ: $it" },
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ") ?: "AQI Station"
}

private fun setupMap(
    map: MapLibreMap,
    mapView: MapView,
    hasLocationPermission: Boolean,
    onStyleReady: () -> Unit = {}
) {
    val apiKey = BuildConfig.MAPTILER_API_KEY
    val styleUrl = buildStyleUrl(apiKey)

    map.setStyle(styleUrl) { _ ->
        if (hasLocationPermission) {
            enableMyLocation(map, mapView)
        }
        onStyleReady()
    }

    val vietnamBounds = LatLngBounds.Builder()
        .include(LatLng(23.39, 109.46))
        .include(LatLng(8.18, 102.14))
        .build()
    map.setLatLngBoundsForCameraTarget(vietnamBounds)

    val hanoiPosition = CameraPosition.Builder()
        .target(LatLng(21.0285, 105.8542))
        .zoom(12.0)
        .build()
    map.cameraPosition = hanoiPosition

    map.uiSettings.apply {
        isRotateGesturesEnabled = true
        isTiltGesturesEnabled = true
        isZoomGesturesEnabled = true
        isScrollGesturesEnabled = true
    }
}

private fun enableMyLocation(map: MapLibreMap, mapView: MapView) {
    val context = mapView.context
    if (!hasLocationPermission(context)) return

    val style = map.style ?: return
    val activationOptions = LocationComponentActivationOptions.builder(context, style)
        .useDefaultLocationEngine(true)
        .build()

    map.locationComponent.apply {
        activateLocationComponent(activationOptions)
        isLocationComponentEnabled = true
        cameraMode = CameraMode.TRACKING
        renderMode = RenderMode.COMPASS
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

private const val MAPLIBRE_DEMO_STYLE = "https://demotiles.maplibre.org/style.json"

private fun buildStyleUrl(apiKey: String): String =
    if (apiKey.isBlank()) MAPLIBRE_DEMO_STYLE
    else "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"
