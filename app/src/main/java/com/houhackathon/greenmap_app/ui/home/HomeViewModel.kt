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
import android.location.Geocoder
import androidx.lifecycle.viewModelScope
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.core.location.LocationProvider
import com.houhackathon.greenmap_app.domain.usecase.GetWeatherForecastUseCase
import com.houhackathon.greenmap_app.domain.model.AiProvider
import com.houhackathon.greenmap_app.domain.usecase.GetAiWeatherInsightsUseCase
import com.houhackathon.greenmap_app.domain.usecase.GetCachedAiInsightUseCase
import com.houhackathon.greenmap_app.domain.usecase.SaveAiInsightUseCase
import com.houhackathon.greenmap_app.domain.model.AiInsightCache
import com.houhackathon.greenmap_app.extension.flow.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val AI_CACHE_MAX_AGE_MILLIS = 15 * 60 * 1000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWeatherForecastUseCase: GetWeatherForecastUseCase,
    private val getAiWeatherInsightsUseCase: GetAiWeatherInsightsUseCase,
    private val getCachedAiInsightUseCase: GetCachedAiInsightUseCase,
    private val saveAiInsightUseCase: SaveAiInsightUseCase,
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
            is HomeIntent.SelectAiProvider -> {
                _viewState.update { it.copy(selectedAiProvider = intent.provider) }
            }
            is HomeIntent.AnalyzeWeatherWithAi -> loadAiInsights(intent.provider, forceRefresh = intent.forceRefresh)
        }
    }

    private fun fetchCurrentLocation() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null) }
            when (val result = locationProvider.getCurrentLocation()) {
                is Result.Success -> {
                    val (lat, lon) = result.data
                    val locationName = resolveLocationName(lat, lon)
                    _viewState.update {
                        it.copy(
                            lat = lat.toString(),
                            lon = lon.toString(),
                            locationName = locationName,
                            aiError = null,
                            aiAnalysis = null,
                            aiModel = null
                        )
                    }
                    viewModelScope.launch {
                        tryLoadCachedAiInsight(
                            provider = _viewState.value.selectedAiProvider,
                            lat = lat,
                            lon = lon
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
                is Result.Success -> {
                    _viewState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            forecast = result.data
                        )
                    }
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

    private fun loadAiInsights(
        providerOverride: AiProvider? = null,
        latOverride: Double? = null,
        lonOverride: Double? = null,
        forceRefresh: Boolean = false,
    ) {
        val provider = providerOverride ?: _viewState.value.selectedAiProvider
        val lat = latOverride ?: _viewState.value.lat.toDoubleOrNull()
        val lon = lonOverride ?: _viewState.value.lon.toDoubleOrNull()

        if (lat == null || lon == null) {
            val message = appContext.getString(R.string.weather_no_location)
            _viewState.update { it.copy(aiError = message, isAiLoading = false) }
            sendEvent(HomeEvent.ShowToast(message))
            return
        }

        viewModelScope.launch {
            if (!forceRefresh && tryLoadCachedAiInsight(provider, lat, lon)) {
                return@launch
            }

            _viewState.update {
                it.copy(
                    isAiLoading = true,
                    aiError = null,
                    selectedAiProvider = provider
                )
            }
            when (val result = getAiWeatherInsightsUseCase(provider, lat, lon)) {
                is Result.Success -> {
                    val response = result.data
                    val resolvedProvider = AiProvider.fromRaw(response.provider) ?: provider
                    val locationName = _viewState.value.locationName ?: resolveLocationName(lat, lon)
                    _viewState.update {
                        it.copy(
                            isAiLoading = false,
                            aiAnalysis = response.analysis,
                            aiModel = response.model,
                            selectedAiProvider = resolvedProvider,
                            aiError = null,
                            locationName = locationName
                        )
                    }
                    saveAiInsightUseCase(
                        AiInsightCache(
                            provider = resolvedProvider,
                            lat = lat,
                            lon = lon,
                            model = response.model,
                            analysis = response.analysis,
                            locationName = locationName,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                is Result.Error -> {
                    val message = result.exception.message ?: appContext.getString(R.string.ai_error_generic)
                    _viewState.update {
                        it.copy(
                            isAiLoading = false,
                            aiError = message
                        )
                    }
                    sendEvent(HomeEvent.ShowToast(message))
                }
                Result.Loading -> _viewState.update { it.copy(isAiLoading = true) }
            }
        }
    }

    private suspend fun tryLoadCachedAiInsight(
        provider: AiProvider,
        lat: Double,
        lon: Double,
    ): Boolean {
        val cached = getCachedAiInsightUseCase(provider, lat, lon, AI_CACHE_MAX_AGE_MILLIS) ?: return false
        _viewState.update {
            it.copy(
                isAiLoading = false,
                aiAnalysis = cached.analysis,
                aiModel = cached.model,
                aiError = null,
                selectedAiProvider = cached.provider,
                locationName = it.locationName ?: cached.locationName
            )
        }
        return true
    }

    private suspend fun resolveLocationName(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(appContext, Locale.getDefault())
            val results = geocoder.getFromLocation(lat, lon, 1)
            results?.firstOrNull()?.let { address ->
                listOfNotNull(
                    address.subLocality,
                    address.locality ?: address.adminArea
                ).joinToString(", ").ifBlank { address.featureName }
            }
        }.getOrNull()
    }
}
