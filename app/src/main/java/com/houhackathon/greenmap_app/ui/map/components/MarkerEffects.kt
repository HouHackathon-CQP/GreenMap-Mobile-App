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

package com.houhackathon.greenmap_app.ui.map.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.ui.map.AqiStationMarker
import com.houhackathon.greenmap_app.ui.map.LocationPoiMarker
import com.houhackathon.greenmap_app.ui.map.MapLayer
import com.houhackathon.greenmap_app.ui.map.MarkerInfo
import com.houhackathon.greenmap_app.ui.map.WeatherStationMarker
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

@Composable
fun WeatherMarkersEffect(
    weatherStations: List<WeatherStationMarker>,
    mapLibreMap: MapLibreMap?,
    markers: MutableList<Marker>,
    selectedLayers: Set<MapLayer>,
    markerInfoMap: MutableMap<Marker, MarkerInfo>,
    refreshKey: Int,
) {
    LaunchedEffect(weatherStations, mapLibreMap, selectedLayers, refreshKey) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val enabled = selectedLayers.contains(MapLayer.WEATHER)
        markers.forEach {
            map.removeAnnotation(it)
            markerInfoMap.remove(it)
        }
        markers.clear()
        if (!enabled) return@LaunchedEffect
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
            markerInfoMap[marker] = MarkerInfo(
                title = station.name,
                subtitle = station.weatherType,
                description = station.temperature?.let { "Nhiệt độ ${"%.1f".format(it)}°C" },
                category = "Thời tiết",
                lat = station.lat,
                lon = station.lon
            )
        }
    }
}

@Composable
fun AqiMarkersEffect(
    aqiStations: List<AqiStationMarker>,
    mapLibreMap: MapLibreMap?,
    markers: MutableList<Marker>,
    aqiIcon: Icon?,
    selectedLayers: Set<MapLayer>,
    markerInfoMap: MutableMap<Marker, MarkerInfo>,
    refreshKey: Int,
) {
    LaunchedEffect(aqiStations, mapLibreMap, selectedLayers, refreshKey) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val enabled = selectedLayers.contains(MapLayer.AQI)
        markers.forEach {
            map.removeAnnotation(it)
            markerInfoMap.remove(it)
        }
        markers.clear()
        if (!enabled) return@LaunchedEffect
        aqiStations.forEach { station ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(station.lat, station.lon))
                    .title(station.name)
                    .snippet(buildAqiSnippet(station))
                    .icon(aqiIcon)
            )
            markers.add(marker)
            markerInfoMap[marker] = MarkerInfo(
                title = station.name,
                subtitle = buildAqiSnippet(station),
                description = station.aqiCategory?.label,
                category = "AQI",
                lat = station.lat,
                lon = station.lon
            )
        }
    }
}

@Composable
fun PoiMarkersEffect(
    poiStations: List<LocationPoiMarker>,
    mapLibreMap: MapLibreMap?,
    markers: MutableMap<LocationType, MutableList<Marker>>,
    iconMap: Map<LocationType, Icon>,
    selectedLayers: Set<MapLayer>,
    markerInfoMap: MutableMap<Marker, MarkerInfo>,
    refreshKey: Int,
) {
    LaunchedEffect(poiStations, mapLibreMap, selectedLayers, refreshKey) {
        val map = mapLibreMap ?: return@LaunchedEffect

        // Remove markers for deselected layers
        val activeTypes = selectedLayers.mapNotNull { it.locationType }.toSet()
        val deselectedTypes = markers.keys.toSet() - activeTypes
        deselectedTypes.forEach { type ->
            markers[type]?.forEach {
                map.removeAnnotation(it)
                markerInfoMap.remove(it)
            }
            markers.remove(type)
        }

        // Update markers per selected type
        activeTypes.forEach { type ->
            markers[type]?.forEach {
                map.removeAnnotation(it)
                markerInfoMap.remove(it)
            }
            markers[type] = mutableListOf()

            val entries = poiStations.filter { it.type == type }
            entries.forEach { station ->
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(station.lat, station.lon))
                        .title(station.name)
                        .snippet(buildPoiSnippet(station))
                        .icon(iconMap[station.type])
                )
                markers[type]?.add(marker)
                markerInfoMap[marker] = MarkerInfo(
                    title = station.name,
                    subtitle = station.type.displayName,
                    description = station.description ?: station.dataSource,
                    category = "POI",
                    lat = station.lat,
                    lon = station.lon
                )
            }
        }
    }
}

private fun buildAqiSnippet(station: AqiStationMarker): String {
    val parts = listOfNotNull(
        station.aqi?.let { "VN AQI: $it" },
        station.aqiCategory?.label?.let { "Level: $it" },
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ") ?: "AQI Station"
}

private fun buildPoiSnippet(marker: LocationPoiMarker): String {
    val status = marker.description?.takeIf { it.isNotBlank() }
    val parts = listOfNotNull(
        "Loại: ${marker.type.displayName}",
        status
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ") ?: marker.type.displayName
}
