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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.houhackathon.greenmap_app.R
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
import com.houhackathon.greenmap_app.ui.theme.Charcoal
import com.houhackathon.greenmap_app.ui.theme.Leaf700
import org.maplibre.android.annotations.Icon
import org.maplibre.android.maps.MapLibreMap
import toMapLibreIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    poiIconOverrides: Map<LocationType, Icon>? = null,
) {
    val viewModel: MapViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val markerStore = remember { MapViewHolder.markerStore }
    val mapView = remember { MapViewHolder.getOrCreate(context) }
    var isMapReady by remember { mutableStateOf(MapViewHolder.isInitialized()) }
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val weatherMarkers = markerStore.weatherMarkers
    val aqiMarkers = markerStore.aqiMarkers
    val poiMarkers = markerStore.poiMarkers
    val aqiIcon = remember {
        context.toMapLibreIcon(
            R.drawable.ic_aqi,
            Leaf700
        )
    }
    val poiIcons = remember(poiIconOverrides) { poiIconOverrides ?: buildPoiIconMap(context) }
    var selectedLayers by rememberSaveable(stateSaver = mapLayerSetSaver) {
        mutableStateOf(defaultSelectedLayers)
    }
    val markerInfoMap = markerStore.markerInfoMap
    var selectedMarkerInfo by remember { mutableStateOf<MarkerInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var markerRefreshVersion by remember { mutableStateOf(0) }

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
            map.setOnMarkerClickListener { marker ->
                selectedMarkerInfo = markerInfoMap[marker]
                selectedMarkerInfo != null
            }
            markerRefreshVersion++
        },
        onReuse = { map ->
            map.setOnMarkerClickListener { marker ->
                selectedMarkerInfo = markerInfoMap[marker]
                selectedMarkerInfo != null
            }
            markerRefreshVersion++
        }
    )
    WeatherMarkersEffect(
        weatherStations = viewState.weatherStations,
        mapLibreMap = mapLibreMap,
        markers = weatherMarkers,
        selectedLayers = selectedLayers,
        markerInfoMap = markerInfoMap,
        refreshKey = markerRefreshVersion
    )
    AqiMarkersEffect(
        aqiStations = viewState.aqiStations,
        mapLibreMap = mapLibreMap,
        markers = aqiMarkers,
        aqiIcon = aqiIcon,
        selectedLayers = selectedLayers,
        markerInfoMap = markerInfoMap,
        refreshKey = markerRefreshVersion
    )
    PoiMarkersEffect(
        poiStations = viewState.poiStations,
        mapLibreMap = mapLibreMap,
        markers = poiMarkers,
        iconMap = poiIcons,
        selectedLayers = selectedLayers,
        markerInfoMap = markerInfoMap,
        refreshKey = markerRefreshVersion
    )
    MapLifecycleHandler(mapView)

    MapContent(
        modifier = modifier,
        mapView = mapView,
        isMapReady = isMapReady,
        errorMessage = viewState.error
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchBarOverlay(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { searchQuery = it }
            )
            QuickFilterRow(
                selected = selectedLayers,
                onToggle = { layer ->
                    selectedLayers = toggleLayer(selectedLayers, layer)
                }
            )
        }
    }

    LaunchedEffect(selectedMarkerInfo) {
        if (selectedMarkerInfo != null) {
            sheetState.show()
        } else if (sheetState.isVisible) {
            sheetState.hide()
        }
    }

    selectedMarkerInfo?.let { info ->
        MarkerInfoSheet(
            info = info,
            onDismiss = { selectedMarkerInfo = null },
            sheetState = sheetState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkerInfoSheet(
    info: MarkerInfo,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = info.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Charcoal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!info.category.isNullOrBlank()) {
                        Text(
                            text = info.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (!info.subtitle.isNullOrBlank()) {
                Text(
                    text = info.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!info.description.isNullOrBlank()) {
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đóng")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        androidx.compose.material3.TextField(
            value = query,
            onValueChange = { onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Thử tìm trạm sạc, ATM...", color = Charcoal.copy(alpha = 0.6f)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Charcoal
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearch(query) }
                )
            },
            colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
    }
}

@Composable
private fun QuickFilterRow(
    selected: Set<MapLayer>,
    onToggle: (MapLayer) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MapFilterBar(
            selected = selected,
            onToggle = onToggle,
            modifier = Modifier
        )
    }
}

private val defaultSelectedLayers = setOf(MapLayer.WEATHER, MapLayer.AQI)

private val mapLayerSetSaver = listSaver<Set<MapLayer>, String>(
    save = { layers -> layers.map(MapLayer::name) },
    restore = { names ->
        names.mapNotNull { name ->
            runCatching { MapLayer.valueOf(name) }.getOrNull()
        }.toSet()
    }
)

private fun toggleLayer(current: Set<MapLayer>, layer: MapLayer): Set<MapLayer> =
    if (current.contains(layer)) current - layer else current + layer
