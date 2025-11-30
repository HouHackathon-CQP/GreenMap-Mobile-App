package com.houhackathon.greenmap_app.ui.map

import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState

data class MapViewState (
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
): MviViewState

sealed class MapIntent : MviIntent {

}

sealed class MapEvent : MviSingleEvent {

}




