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

import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState

data class HomeViewState(
    val isServerLoading: Boolean = false,
    val isServerError: Boolean = false,
    val serverStatus: String? = null,
    val isDatabaseLoading: Boolean = false,
    val isDatabaseError: Boolean = false,
    val databaseStatus: String? = null,
) : MviViewState

sealed class HomeIntent : MviIntent {
    data object CheckServer : HomeIntent()
    data object CheckDatabase : HomeIntent()
    data object NavigateToMap : HomeIntent()
}

sealed class HomeEvent : MviSingleEvent {
    data class ShowToast(val message: String) : HomeEvent()
    data object NavigateMap : HomeEvent()
}
