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

import com.houhackathon.greenmap_app.data.remote.dto.WeatherForecastResponse
import com.houhackathon.greenmap_app.domain.model.AiProvider
import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState

data class HomeViewState(
    val lat: String = "",
    val lon: String = "",
    val locationName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val forecast: WeatherForecastResponse? = null,
    val selectedAiProvider: AiProvider = AiProvider.GEMINI,
    val aiModel: String? = null,
    val aiAnalysis: String? = null,
    val isAiLoading: Boolean = false,
    val aiError: String? = null,
) : MviViewState

sealed class HomeIntent : MviIntent {
    data object UseCurrentLocation : HomeIntent()
    data object LoadForecast : HomeIntent()
    data object RefreshForecast : HomeIntent()
    data object NavigateToMap : HomeIntent()
    data class SelectAiProvider(val provider: AiProvider) : HomeIntent()
    data class AnalyzeWeatherWithAi(
        val provider: AiProvider? = null,
        val forceRefresh: Boolean = false,
    ) : HomeIntent()
}

sealed class HomeEvent : MviSingleEvent {
    data class ShowToast(val message: String) : HomeEvent()
    data object NavigateMap : HomeEvent()
}
