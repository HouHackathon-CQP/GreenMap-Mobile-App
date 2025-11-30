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
