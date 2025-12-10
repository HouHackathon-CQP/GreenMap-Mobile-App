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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.domain.model.DirectionPlan
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.ui.map.components.AqiMarkersEffect
import com.houhackathon.greenmap_app.ui.map.components.CameraFollowEffect
import com.houhackathon.greenmap_app.ui.map.components.DirectionOverlayEffect
import com.houhackathon.greenmap_app.ui.map.components.LocationPermissionEffect
import com.houhackathon.greenmap_app.ui.map.components.MapContent
import com.houhackathon.greenmap_app.ui.map.components.MapEventHandler
import com.houhackathon.greenmap_app.ui.map.components.MapFilterBar
import com.houhackathon.greenmap_app.ui.map.components.MapInitializer
import com.houhackathon.greenmap_app.ui.map.components.MapLifecycleHandler
import com.houhackathon.greenmap_app.ui.map.components.PoiMarkersEffect
import com.houhackathon.greenmap_app.ui.map.components.WeatherMarkersEffect
import com.houhackathon.greenmap_app.ui.theme.Charcoal
import org.maplibre.android.annotations.Icon
import org.maplibre.android.maps.MapLibreMap

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
    val aqiIconFactory = remember { AqiIconFactory(context) }
    val weatherIconFactory = remember { WeatherIconFactory(context) }
    val poiIconFactory = remember { PoiIconFactory(context) }
    val poiIconResolver = remember(poiIconOverrides, poiIconFactory) {
        { type: LocationType -> poiIconOverrides?.get(type) ?: poiIconFactory.iconFor(type) }
    }
    val directionStore = remember { MapViewHolder.directionStore }
    var selectedLayers by rememberSaveable(stateSaver = mapLayerSetSaver) {
        mutableStateOf(defaultSelectedLayers)
    }
    val markerInfoMap = markerStore.markerInfoMap
    var selectedMarkerInfo by remember { mutableStateOf<MarkerInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var markerRefreshVersion by remember { mutableStateOf(0) }

    MapEventHandler(viewModel)
    LocationPermissionEffect(
        hasLocationPermission = hasLocationPermission,
        mapView = mapView,
        onPermissionChanged = { hasLocationPermission = it }
    )
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.processIntent(MapIntent.StartLocationUpdates)
        } else {
            viewModel.processIntent(MapIntent.StopLocationUpdates)
        }
    }
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
        weatherIconFactory = weatherIconFactory,
        selectedLayers = selectedLayers,
        markerInfoMap = markerInfoMap,
        refreshKey = markerRefreshVersion
    )
    AqiMarkersEffect(
        aqiStations = viewState.aqiStations,
        mapLibreMap = mapLibreMap,
        markers = aqiMarkers,
        aqiIconFactory = aqiIconFactory,
        selectedLayers = selectedLayers,
        markerInfoMap = markerInfoMap,
        refreshKey = markerRefreshVersion
    )
    PoiMarkersEffect(
        poiStations = viewState.poiStations,
        mapLibreMap = mapLibreMap,
        markers = poiMarkers,
        iconProvider = poiIconResolver,
        selectedLayers = selectedLayers,
        markerInfoMap = markerInfoMap,
        refreshKey = markerRefreshVersion
    )
    DirectionOverlayEffect(
        directionPlan = viewState.directionPlan,
        mapLibreMap = mapLibreMap,
        store = directionStore,
        markerInfoMap = markerInfoMap,
        poiIconProvider = poiIconResolver,
        refreshKey = markerRefreshVersion
    )
    CameraFollowEffect(
        mapLibreMap = mapLibreMap,
        location = viewState.currentLocation,
        bearing = viewState.currentBearing,
        enabled = viewState.currentLocation != null,
        navigationMode = viewState.directionPlan != null
    )
    MapLifecycleHandler(mapView)

    MapContent(
        modifier = modifier,
        mapView = mapView,
        isMapReady = isMapReady,
        errorMessage = viewState.error
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            MapOverlayColumn(
                isDirectionEnabled = viewState.isDirectionEnabled,
                directionQuery = viewState.directionQuery,
                isDirectionLoading = viewState.isDirectionLoading,
                directionPlan = viewState.directionPlan,
                directionError = viewState.directionError,
                remainingDistance = viewState.remainingDistance,
                remainingDuration = viewState.remainingDuration,
                selectedLayers = selectedLayers,
                onDirectionQueryChange = {
                    viewModel.processIntent(MapIntent.UpdateDirectionQuery(it))
                    if (it.isBlank()) {
                        viewModel.processIntent(MapIntent.ClearDirections)
                    }
                },
                onSearchDirections = { viewModel.processIntent(MapIntent.FindDirections(it)) },
                onToggleLayer = { layer -> selectedLayers = toggleLayer(selectedLayers, layer) },
                onNavigateClick = {
                    mapLibreMap?.focusDirectionRoute(viewState.directionPlan)
                },
                onCancelRoute = { viewModel.processIntent(MapIntent.ClearDirections) },
                modifier = Modifier.fillMaxSize()
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
            sheetState = sheetState,
            showDirections = viewState.isDirectionEnabled
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkerInfoSheet(
    info: MarkerInfo,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    showDirections: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        val context = LocalContext.current
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

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đóng")
            }
        }
    }
}

@Composable
private fun MapOverlayColumn(
    isDirectionEnabled: Boolean,
    directionQuery: String,
    isDirectionLoading: Boolean,
    directionPlan: DirectionPlan?,
    directionError: String?,
    remainingDistance: Double?,
    remainingDuration: Double?,
    selectedLayers: Set<MapLayer>,
    onDirectionQueryChange: (String) -> Unit,
    onSearchDirections: (String) -> Unit,
    onToggleLayer: (MapLayer) -> Unit,
    onNavigateClick: () -> Unit,
    onCancelRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isDirectionEnabled) {
            SearchBarOverlay(
                query = directionQuery,
                onQueryChange = onDirectionQueryChange,
                onSearch = onSearchDirections,
                isSearching = isDirectionLoading,
                onClear = if (directionQuery.isNotBlank() || directionPlan != null) {
                    {
                        onDirectionQueryChange("")
                        onCancelRoute()
                    }
                } else null
            )
        }
        QuickFilterRow(
            selected = selectedLayers,
            onToggle = onToggleLayer
        )
        if (isDirectionEnabled) {
            Spacer(modifier = Modifier.weight(1f, fill = true))
            DirectionSummaryCard(
                plan = directionPlan,
                error = directionError,
                isLoading = isDirectionLoading,
                remainingDistance = remainingDistance,
                remainingDuration = remainingDuration,
                onNavigateClick = onNavigateClick,
                onCancelRoute = onCancelRoute,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DirectionSummaryCard(
    plan: DirectionPlan?,
    error: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    remainingDistance: Double? = null,
    remainingDuration: Double? = null,
    onNavigateClick: (() -> Unit)? = null,
    onCancelRoute: (() -> Unit)? = null,
) {
    if (plan == null && error.isNullOrBlank() && !isLoading) return
    var collapsed by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) Column@{
            when {
                isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = stringResource(id = R.string.direction_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Charcoal
                        )
                    }
                }

                !error.isNullOrBlank() -> {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                plan != null -> {
                    HeaderRow(
                        title = plan.destination.name ?: formatLatLon(
                            plan.destination.lat,
                            plan.destination.lon
                        ),
                        collapsed = collapsed,
                        onToggleCollapse = { collapsed = !collapsed },
                        onCancel = onCancelRoute
                    )
                    if (collapsed) return@Column
                    plan.summary?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    formatDistance(plan.route.distanceMeters)?.let {
                        Text(
                            text = "Khoảng cách: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    formatDuration(plan.route.durationSeconds)?.let {
                        Text(
                            text = "Thời gian dự kiến: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    val remainDistanceText = formatDistance(remainingDistance)
                    val remainDurationText = formatDuration(remainingDuration)
                    if (remainDistanceText != null || remainDurationText != null) {
                        Text(
                            text = listOfNotNull(
                                remainDistanceText?.let { rd -> "Còn lại: $rd" },
                                remainDurationText?.let { rt -> "~$rt" }
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (plan.viaPois.isNotEmpty()) {
                        Text(
                            text = "Qua: ${plan.viaPois.joinToString(" \u2022 ") { via -> via.name ?: formatLatLon(via.lat, via.lon) }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    onNavigateClick?.let { onNavigate ->
                        Button(
                            onClick = {
                                onNavigate()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bắt đầu theo dõi tuyến trên bản đồ")
                        }
                    }
                    onCancelRoute?.let { cancel ->
                        Button(
                            onClick = cancel,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Huỷ lộ trình")
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onCancel: (() -> Unit)?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Charcoal
            )
        }
        onCancel?.let { cancel ->
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Huỷ lộ trình",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { cancel() }
            )
        }
        Icon(
            painter = if (collapsed) painterResource(R.drawable.expand_all) else painterResource(R.drawable.collapse_content),
            contentDescription = if (collapsed) "Mở rộng" else "Thu gọn",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggleCollapse() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    isSearching: Boolean = false,
    onClear: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        TextField(
            value = query,
            onValueChange = { onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.direction_placeholder),
                    color = Charcoal.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Charcoal
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onClear != null) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Xoá tìm kiếm",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onClear() }
                        )
                    }
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onSearch(query) }
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            colors = TextFieldDefaults.textFieldColors(
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

private val defaultSelectedLayers = setOf(MapLayer.WEATHER)

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

// Intentionally left for potential future use without UI surface.
private fun openGoogleMapsDirections(
    context: Context,
    lat: Double,
    lon: Double,
    label: String?,
) {
    // no-op: launcher removed from UI
}
