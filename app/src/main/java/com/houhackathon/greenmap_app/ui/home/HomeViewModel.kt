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

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.core.location.LocationProvider
import com.houhackathon.greenmap_app.domain.usecase.GetWeatherForecastUseCase
import com.houhackathon.greenmap_app.extension.flow.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWeatherForecastUseCase: GetWeatherForecastUseCase,
    private val locationProvider: LocationProvider,
    @ApplicationContext private val appContext: Context,
) : BaseMviViewModel<HomeIntent, HomeViewState, HomeEvent>() {

    private val _viewState = MutableStateFlow(HomeViewState())
    override val viewState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            intentSharedFlow.collect(::handleIntent)
        }
    }

    private fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.UseCurrentLocation -> fetchCurrentLocation()
            HomeIntent.LoadForecast, HomeIntent.RefreshForecast -> loadForecast()
            HomeIntent.NavigateToMap -> sendEvent(HomeEvent.NavigateMap)
        }
    }

    private fun fetchCurrentLocation() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null) }
            when (val result = locationProvider.getCurrentLocation()) {
                is Result.Success -> {
                    val (lat, lon) = result.data
                    _viewState.update {
                        it.copy(
                            lat = lat.toString(),
                            lon = lon.toString()
                        )
                    }
                    loadForecast(latOverride = lat, lonOverride = lon)
                }
                is Result.Error -> {
                    val message = result.exception.message ?: appContext.getString(R.string.weather_location_unavailable)
                    _viewState.update { it.copy(isLoading = false, error = message) }
                    sendEvent(HomeEvent.ShowToast(message))
                }
                Result.Loading -> _viewState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun loadForecast(latOverride: Double? = null, lonOverride: Double? = null) {
        val lat = latOverride ?: _viewState.value.lat.toDoubleOrNull()
        val lon = lonOverride ?: _viewState.value.lon.toDoubleOrNull()

        if (lat == null || lon == null) {
            val message = appContext.getString(R.string.weather_no_location)
            _viewState.update { it.copy(error = message) }
            sendEvent(HomeEvent.ShowToast(message))
            return
        }

        viewModelScope.launch {
            _viewState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }
            when (val result = getWeatherForecastUseCase(lat, lon)) {
                is Result.Success -> _viewState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        forecast = result.data
                    )
                }
                is Result.Error -> {
                    _viewState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: appContext.getString(R.string.weather_error_generic),
                            forecast = null
                        )
                    }
                    sendEvent(
                        HomeEvent.ShowToast(
                            result.exception.message ?: appContext.getString(R.string.weather_error_generic)
                        )
                    )
                }
                Result.Loading -> _viewState.update { it.copy(isLoading = true) }
            }
        }
    }
}
