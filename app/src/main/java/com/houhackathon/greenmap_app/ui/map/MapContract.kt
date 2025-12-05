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

data class MapViewState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stations: List<WeatherStationMarker> = emptyList(),
) : MviViewState

data class WeatherStationMarker(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val temperature: Double?,
    val weatherType: String?,
)

sealed class MapIntent : MviIntent {
    data object LoadStations : MapIntent()
    data object RefreshStations : MapIntent()
}

sealed class MapEvent : MviSingleEvent {
    data class ShowToast(val message: String) : MapEvent()
}
