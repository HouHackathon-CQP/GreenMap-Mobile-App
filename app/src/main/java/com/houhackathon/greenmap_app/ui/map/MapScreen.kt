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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.ui.map.components.AqiMarkersEffect
import com.houhackathon.greenmap_app.ui.map.components.LocationPermissionEffect
import com.houhackathon.greenmap_app.ui.map.components.MapContent
import com.houhackathon.greenmap_app.ui.map.components.MapEventHandler
import com.houhackathon.greenmap_app.ui.map.components.MapFilterBar
import com.houhackathon.greenmap_app.ui.map.components.MapInitializer
import com.houhackathon.greenmap_app.ui.map.components.MapLifecycleHandler
import com.houhackathon.greenmap_app.ui.map.components.PoiMarkersEffect
import com.houhackathon.greenmap_app.ui.map.components.WeatherMarkersEffect
import com.houhackathon.greenmap_app.ui.theme.Leaf700
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.maps.MapLibreMap
import toMapLibreIcon

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    poiIconOverrides: Map<LocationType, Icon>? = null,
) {
    val viewModel: MapViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current
    val mapView = remember { MapViewHolder.getOrCreate(context) }
    var isMapReady by remember { mutableStateOf(MapViewHolder.isInitialized()) }
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val weatherMarkers = remember { mutableListOf<Marker>() }
    val aqiMarkers = remember { mutableListOf<Marker>() }
    val poiMarkers = remember { mutableStateMapOf<LocationType, MutableList<Marker>>() }
    val aqiIcon = remember {
        context.toMapLibreIcon(
            com.houhackathon.greenmap_app.R.drawable.ic_aqi,
            Leaf700
        )
    }
    val poiIcons = remember(poiIconOverrides) { poiIconOverrides ?: buildPoiIconMap(context) }
    var selectedLayers by remember { mutableStateOf(setOf(MapLayer.WEATHER, MapLayer.AQI)) }

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
        onMapPrepared = { isMapReady = true },
        onSetup = { map ->
            setupMap(map, mapView, hasLocationPermission) {
                mapLibreMap = map
            }
        }
    )
    WeatherMarkersEffect(
        weatherStations = viewState.weatherStations,
        mapLibreMap = mapLibreMap,
        markers = weatherMarkers,
        selectedLayers = selectedLayers
    )
    AqiMarkersEffect(
        aqiStations = viewState.aqiStations,
        mapLibreMap = mapLibreMap,
        markers = aqiMarkers,
        aqiIcon = aqiIcon,
        selectedLayers = selectedLayers
    )
    PoiMarkersEffect(
        poiStations = viewState.poiStations,
        mapLibreMap = mapLibreMap,
        markers = poiMarkers,
        iconMap = poiIcons,
        selectedLayers = selectedLayers
    )
    MapLifecycleHandler(mapView)

    MapContent(
        modifier = modifier,
        mapView = mapView,
        isMapReady = isMapReady,
        errorMessage = viewState.error
    ) {
        MapFilterBar(
            selected = selectedLayers,
            onToggle = { layer ->
                selectedLayers = if (selectedLayers.contains(layer)) {
                    selectedLayers - layer
                } else {
                    selectedLayers + layer
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
