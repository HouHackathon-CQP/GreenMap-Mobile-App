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
import com.houhackathon.greenmap_app.domain.usecase.GetHanoiWeatherUseCase
import com.houhackathon.greenmap_app.extension.flow.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getHanoiWeatherUseCase: GetHanoiWeatherUseCase,
    @ApplicationContext private val appContext: Context,
) : BaseMviViewModel<MapIntent, MapViewState, MapEvent>() {

    private val _viewState = MutableStateFlow(MapViewState())
    override val viewState: StateFlow<MapViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch { intentSharedFlow.collect(::handleIntent) }
        processIntent(MapIntent.LoadStations)
    }

    private fun handleIntent(intent: MapIntent) {
        when (intent) {
            MapIntent.LoadStations, MapIntent.RefreshStations -> loadStations()
        }
    }

    private fun loadStations() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null) }
            when (val result = getHanoiWeatherUseCase()) {
                is Result.Success -> {
                    _viewState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            stations = result.data.data.mapNotNull { dto ->
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
                        )
                    }
                }
                is Result.Error -> {
                    val message = result.exception.message ?: appContext.getString(R.string.weather_error_generic)
                    _viewState.update { it.copy(isLoading = false, error = message, stations = emptyList()) }
                    sendEvent(MapEvent.ShowToast(message))
                }
                Result.Loading -> _viewState.update { it.copy(isLoading = true) }
            }
        }
    }
}
