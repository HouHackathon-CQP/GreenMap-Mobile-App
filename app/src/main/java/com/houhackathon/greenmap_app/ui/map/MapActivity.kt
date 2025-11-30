package com.houhackathon.greenmap_app.ui.map

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.houhackathon.greenmap_app.core.activity.ComposeMviActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapActivity : ComposeMviActivity<MapIntent, MapViewState, MapEvent, MapViewModel>() {

    override val viewModel: MapViewModel by viewModels()

    @Composable
    override fun SetContentView(viewState: MapViewState) {
        MapLibreScreen()
    }
}
