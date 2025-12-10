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
import androidx.lifecycle.viewModelScope
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.core.location.LocationProvider
import com.houhackathon.greenmap_app.core.remoteconfig.RemoteConfigKeys
import com.houhackathon.greenmap_app.core.remoteconfig.RemoteConfigManager
import com.houhackathon.greenmap_app.data.remote.dto.AqiHanoiResponse
import com.houhackathon.greenmap_app.data.remote.dto.LocationDto
import com.houhackathon.greenmap_app.data.remote.dto.WeatherHanoiResponse
import com.houhackathon.greenmap_app.domain.usecase.GetHanoiAqiUseCase
import com.houhackathon.greenmap_app.domain.usecase.GetHanoiWeatherUseCase
import com.houhackathon.greenmap_app.domain.usecase.GetLocationsUseCase
import com.houhackathon.greenmap_app.domain.usecase.GetDirectionsUseCase
import com.houhackathon.greenmap_app.domain.usecase.GetDrivingRouteUseCase
import com.houhackathon.greenmap_app.extension.flow.Result
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.domain.model.GeoPoint
import com.houhackathon.greenmap_app.domain.model.toGeoPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.Job
import kotlin.math.max

private const val REROUTE_DISTANCE_THRESHOLD_METERS = 30.0
private const val MIN_REROUTE_INTERVAL_MS = 10_000L

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getHanoiWeatherUseCase: GetHanoiWeatherUseCase,
    private val getHanoiAqiUseCase: GetHanoiAqiUseCase,
    private val getLocationsUseCase: GetLocationsUseCase,
    private val remoteConfigManager: RemoteConfigManager,
    private val getDirectionsUseCase: GetDirectionsUseCase,
    private val getDrivingRouteUseCase: GetDrivingRouteUseCase,
    private val locationProvider: LocationProvider,
    @ApplicationContext private val appContext: Context,
) : BaseMviViewModel<MapIntent, MapViewState, MapEvent>() {

    private val _viewState = MutableStateFlow(MapViewState())
    override val viewState: StateFlow<MapViewState> = _viewState.asStateFlow()
    private var locationJob: Job? = null
    private var lastRerouteAt = 0L

    init {
        viewModelScope.launch { intentSharedFlow.collect(::handleIntent) }
        processIntent(MapIntent.LoadStations)
        viewModelScope.launch { refreshDirectionFlag() }
    }

    private fun handleIntent(intent: MapIntent) {
        when (intent) {
            MapIntent.LoadStations, MapIntent.RefreshStations -> loadStations()
            is MapIntent.UpdateDirectionQuery -> _viewState.update { it.copy(directionQuery = intent.value) }
            is MapIntent.FindDirections -> findDirections(intent.question)
            MapIntent.ClearDirections -> clearDirections()
            MapIntent.StartLocationUpdates -> startLocationUpdates()
            MapIntent.StopLocationUpdates -> stopLocationUpdates()
        }
    }

    private suspend fun refreshDirectionFlag() {
        remoteConfigManager.fetchAndActivate()
        val enabled = remoteConfigManager.getBoolean(RemoteConfigKeys.DIRECTION_FLAG)
        _viewState.update { it.copy(isDirectionEnabled = enabled) }
    }

    private fun loadStations() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null) }
            val errors = mutableListOf<String>()
            val (weatherResult, aqiResult, locationResults) = supervisorScope {
                val weatherDeferred = async { getHanoiWeatherUseCase() }
                val aqiDeferred = async { getHanoiAqiUseCase() }
                val locationDeferred = LocationType.values().map { type ->
                    async { type to getLocationsUseCase(type) }
                }
                Triple(weatherDeferred.await(), aqiDeferred.await(), locationDeferred.awaitAll())
            }

            val weatherMarkers = buildWeatherMarkers(weatherResult, errors)
            val aqiMarkers = buildAqiMarkers(aqiResult, errors)
            val locationMarkers = buildLocationMarkers(locationResults, errors)

            _viewState.update {
                it.copy(
                    isLoading = false,
                    error = errors.firstOrNull(),
                    weatherStations = weatherMarkers,
                    aqiStations = aqiMarkers,
                    poiStations = locationMarkers
                )
            }
        }
    }

    private fun buildWeatherMarkers(
        weatherResult: Result<WeatherHanoiResponse>,
        errors: MutableList<String>
    ): List<WeatherStationMarker> {
        return when (weatherResult) {
            is Result.Success -> weatherResult.data.data.mapNotNull { dto ->
                val coords = dto.location?.coordinates
                if (coords == null || coords.size < 2) return@mapNotNull null
                val lon = coords[0]
                val lat = coords[1]
                WeatherStationMarker(
                    id = dto.id ?: "${lat}_${lon}",
                    name = dto.address?.addressRegion ?: dto.address?.addressLocality ?: "Hanoi",
                    lat = lat,
                    lon = lon,
                    temperature = dto.temperature,
                    weatherType = dto.weatherType
                )
            }
            is Result.Error -> {
                val message =
                    weatherResult.exception.message ?: appContext.getString(R.string.weather_error_generic)
                sendEvent(MapEvent.ShowToast(message))
                errors.add(message)
                emptyList()
            }
            Result.Loading -> emptyList()
        }
    }

    private fun buildAqiMarkers(
        aqiResult: Result<AqiHanoiResponse>,
        errors: MutableList<String>
    ): List<AqiStationMarker> {
        return when (aqiResult) {
            is Result.Success -> aqiResult.data.data.mapNotNull { dto ->
                val coords = dto.location?.value?.coordinates
                if (coords == null || coords.size < 2) return@mapNotNull null
                val lon = coords[0]
                val lat = coords[1]
                val pm25Value = dto.pm25?.value
                val aqi = pm25Value?.let { calculateVietnamPm25Aqi(it) }
                AqiStationMarker(
                    id = dto.id ?: "${lat}_${lon}",
                    name = dto.stationName?.value ?: dto.id ?: "AQI Station",
                    lat = lat,
                    lon = lon,
                    pm25 = pm25Value,
                    aqi = aqi?.index,
                    aqiCategory = aqi?.category
                )
            }
            is Result.Error -> {
                val message = aqiResult.exception.message
                    ?: appContext.getString(R.string.weather_error_generic)
                sendEvent(MapEvent.ShowToast(message))
                errors.add(message)
                emptyList()
            }
            Result.Loading -> emptyList()
        }
    }

    private fun buildLocationMarkers(
        locationResults: List<Pair<LocationType, Result<List<LocationDto>>>>,
        errors: MutableList<String>
    ): List<LocationPoiMarker> {
        val locationMarkers = mutableListOf<LocationPoiMarker>()
        locationResults.forEach { (type, result) ->
            when (result) {
                is Result.Success -> locationMarkers += result.data.mapNotNull { dto ->
                    dto.toMarker(type)
                }
                is Result.Error -> {
                    val message = result.exception.message
                        ?: appContext.getString(R.string.location_error_generic, type.displayName)
                    sendEvent(MapEvent.ShowToast(message))
                    errors.add(message)
                }
                Result.Loading -> Unit
            }
        }
        return locationMarkers
    }

    private fun findDirections(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank()) {
            val message = appContext.getString(R.string.direction_query_required)
            _viewState.update { it.copy(directionError = message) }
            sendEvent(MapEvent.ShowToast(message))
            return
        }

        viewModelScope.launch {
            _viewState.update {
                it.copy(
                    directionQuery = trimmed,
                    isDirectionLoading = true,
                    directionError = null,
                    directionPlan = null
                )
            }

            when (val locationResult = locationProvider.getCurrentLocation()) {
                is Result.Success -> {
                    val (lat, lon) = locationResult.data
                    when (val result = getDirectionsUseCase(trimmed, lat, lon)) {
                        is Result.Success -> _viewState.update {
                            it.copy(
                                isDirectionLoading = false,
                                directionPlan = result.data,
                                directionError = null
                            )
                        }

                        is Result.Error -> {
                            val message = resolveDirectionError(result.exception.message)
                            _viewState.update {
                                it.copy(
                                    isDirectionLoading = false,
                                    directionError = message,
                                    directionPlan = null
                                )
                            }
                            sendEvent(MapEvent.ShowToast(message))
                        }

                        Result.Loading -> _viewState.update { it.copy(isDirectionLoading = true) }
                    }
                }

                is Result.Error -> {
                    val message = appContext.getString(R.string.direction_location_unavailable)
                    _viewState.update {
                        it.copy(
                            isDirectionLoading = false,
                            directionError = message,
                            directionPlan = null
                        )
                    }
                    sendEvent(MapEvent.ShowToast(message))
                }

                Result.Loading -> _viewState.update { it.copy(isDirectionLoading = true) }
            }
        }
    }

    private fun clearDirections() {
        _viewState.update {
            it.copy(
                directionPlan = null,
                directionError = null,
                isDirectionLoading = false,
                remainingDistance = null,
                remainingDuration = null
            )
        }
    }

    private fun resolveDirectionError(rawMessage: String?): String {
        val message = rawMessage ?: return appContext.getString(R.string.direction_error_generic)
        return when {
            message.contains("Missing route", ignoreCase = true) -> appContext.getString(R.string.direction_missing_route)
            message.contains("Missing start", ignoreCase = true) -> appContext.getString(R.string.direction_missing_route)
            else -> message.ifBlank { appContext.getString(R.string.direction_error_generic) }
        }
    }

    private fun startLocationUpdates() {
        if (locationJob != null) return
        locationJob = viewModelScope.launch {
            locationProvider.observeLocationUpdates().collect { location ->
                val geoPoint = GeoPoint(lat = location.latitude, lon = location.longitude)
                val bearing = location.bearing
                _viewState.update {
                    it.copy(
                        currentLocation = geoPoint,
                        currentBearing = if (bearing.isNaN()) null else bearing
                    )
                }
                updateRemainingDistance(geoPoint)
                maybeReroute(geoPoint)
            }
        }.also { job ->
            job.invokeOnCompletion { locationJob = null }
        }
    }

    private fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    private fun updateRemainingDistance(current: GeoPoint) {
        val plan = _viewState.value.directionPlan ?: return
        val routePoints = plan.route.coordinates
        if (routePoints.size < 2) return

        val (distanceToRoute, traveled) = distanceToPolyline(current, routePoints)
        val totalDistance = plan.route.distanceMeters ?: routeLength(routePoints)
        val remainingDistance = max(0.0, totalDistance - traveled)
        val remainingDuration = plan.route.durationSeconds?.let { duration ->
            val total = plan.route.distanceMeters ?: totalDistance
            if (total > 0) duration * (remainingDistance / total) else duration
        }
        _viewState.update {
            it.copy(
                remainingDistance = remainingDistance,
                remainingDuration = remainingDuration
            )
        }

        if (distanceToRoute > REROUTE_DISTANCE_THRESHOLD_METERS) {
            // handled in maybeReroute
        }
    }

    private fun maybeReroute(current: GeoPoint) {
        val plan = _viewState.value.directionPlan ?: return
        val destination = plan.destination
        val routePoints = plan.route.coordinates
        if (routePoints.size < 2) return

        val (distanceToRoute, _) = distanceToPolyline(current, routePoints)
        val now = System.currentTimeMillis()
        if (distanceToRoute <= REROUTE_DISTANCE_THRESHOLD_METERS) return
        if (now - lastRerouteAt < MIN_REROUTE_INTERVAL_MS) return

        lastRerouteAt = now
        viewModelScope.launch {
            when (val result = getDrivingRouteUseCase(current, destination.toGeoPoint())) {
                is Result.Success -> {
                    val newPlan = plan.copy(
                        start = plan.start.copy(lat = current.lat, lon = current.lon),
                        route = result.data,
                        viaPois = emptyList()
                    )
                    _viewState.update {
                        it.copy(
                            directionPlan = newPlan,
                            directionError = null
                        )
                    }
                }
                is Result.Error -> {
                    val message = resolveDirectionError(result.exception.message)
                    _viewState.update { it.copy(directionError = message) }
                    sendEvent(MapEvent.ShowToast(message))
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun distanceToPolyline(
        point: GeoPoint,
        polyline: List<GeoPoint>
    ): Pair<Double, Double> {
        var minDistance = Double.MAX_VALUE
        var traveled = 0.0
        var accumulated = 0.0
        polyline.zipWithNext().forEach { (a, b) ->
            val segmentLength = distanceMeters(a, b)
            val (distanceToSegment, projectionFactor) = distancePointToSegment(point, a, b)
            if (distanceToSegment < minDistance) {
                minDistance = distanceToSegment
                traveled = accumulated + (segmentLength * projectionFactor.coerceIn(0.0, 1.0))
            }
            accumulated += segmentLength
        }
        return minDistance to traveled
    }

    private fun routeLength(points: List<GeoPoint>): Double =
        points.zipWithNext().sumOf { (a, b) -> distanceMeters(a, b) }

    private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val sinDLat = kotlin.math.sin(dLat / 2)
        val sinDLon = kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.asin(
            kotlin.math.sqrt(sinDLat * sinDLat + kotlin.math.cos(lat1) * kotlin.math.cos(lat2) * sinDLon * sinDLon)
        )
        return r * c
    }

    private fun distancePointToSegment(p: GeoPoint, a: GeoPoint, b: GeoPoint): Pair<Double, Double> {
        val latRad = Math.toRadians(p.lat)
        val metersPerDegLat = 111132.92 - 559.82 * kotlin.math.cos(2 * latRad) + 1.175 * kotlin.math.cos(4 * latRad)
        val metersPerDegLon = 111412.84 * kotlin.math.cos(latRad) - 93.5 * kotlin.math.cos(3 * latRad)

        val ax = a.lon * metersPerDegLon
        val ay = a.lat * metersPerDegLat
        val bx = b.lon * metersPerDegLon
        val by = b.lat * metersPerDegLat
        val px = p.lon * metersPerDegLon
        val py = p.lat * metersPerDegLat

        val abx = bx - ax
        val aby = by - ay
        val apx = px - ax
        val apy = py - ay
        val abLen2 = abx * abx + aby * aby
        val t = if (abLen2 == 0.0) 0.0 else (apx * abx + apy * aby) / abLen2
        val tClamped = t.coerceIn(0.0, 1.0)
        val projX = ax + abx * tClamped
        val projY = ay + aby * tClamped
        val dx = px - projX
        val dy = py - projY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance to tClamped
    }
}

private fun LocationDto.toMarker(
    fallbackType: LocationType
): LocationPoiMarker? {
    val coords = this.location?.coordinates
    if (coords == null || coords.size < 2) return null
    val lon = coords[0]
    val lat = coords[1]
    val resolvedType = LocationType.fromRaw(this.type) ?: fallbackType
    val markerName = name ?: resolvedType.displayName
    return LocationPoiMarker(
        id = id ?: "${lat}_${lon}_${resolvedType.name}",
        name = markerName,
        type = resolvedType,
        lat = lat,
        lon = lon,
        description = description,
        dataSource = dataSource,
        isEditable = isEditable
    )
}
