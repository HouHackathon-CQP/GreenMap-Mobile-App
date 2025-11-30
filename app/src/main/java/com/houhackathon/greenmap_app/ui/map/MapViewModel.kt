package com.houhackathon.greenmap_app.ui.map

import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() :
    BaseMviViewModel<MapIntent, MapViewState, MapEvent>() {

    private val _viewState = MutableStateFlow(MapViewState())
    override val viewState: StateFlow<MapViewState> = _viewState.asStateFlow()
}
