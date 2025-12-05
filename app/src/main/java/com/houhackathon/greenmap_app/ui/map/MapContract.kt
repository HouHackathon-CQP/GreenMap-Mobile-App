package com.houhackathon.greenmap_app.ui.map

import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState

data class MapViewState(
    val ready: Boolean = false,
) : MviViewState

sealed class MapIntent : MviIntent

sealed class MapEvent : MviSingleEvent

