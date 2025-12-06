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

import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState
import com.houhackathon.greenmap_app.domain.model.LocationType

data class MapViewState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val weatherStations: List<WeatherStationMarker> = emptyList(),
    val aqiStations: List<AqiStationMarker> = emptyList(),
    val poiStations: List<LocationPoiMarker> = emptyList(),
) : MviViewState

data class MarkerInfo(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val category: String? = null,
)

data class WeatherStationMarker(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val temperature: Double?,
    val weatherType: String?,
)

data class AqiStationMarker(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val pm25: Double?,
    val aqi: Int?,
    val aqiCategory: VietnamAqiCategory?,
)

data class LocationPoiMarker(
    val id: String,
    val name: String,
    val type: LocationType,
    val lat: Double,
    val lon: Double,
    val description: String?,
    val dataSource: String?,
    val isEditable: Boolean?,
)

sealed class MapIntent : MviIntent {
    data object LoadStations : MapIntent()
    data object RefreshStations : MapIntent()
}

sealed class MapEvent : MviSingleEvent {
    data class ShowToast(val message: String) : MapEvent()
}
