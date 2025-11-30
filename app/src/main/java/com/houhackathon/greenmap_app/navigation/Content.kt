package com.houhackathon.greenmap_app.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.houhackathon.greenmap_app.Map
import com.houhackathon.greenmap_app.Home
import com.houhackathon.greenmap_app.ui.home.HomeEvent
import com.houhackathon.greenmap_app.ui.home.HomeIntent
import com.houhackathon.greenmap_app.ui.home.HomeScreen
import com.houhackathon.greenmap_app.ui.home.HomeViewModel
import com.houhackathon.greenmap_app.ui.map.MapScreen


fun EntryProviderScope<NavKey>.featureASection(
    onNavigateMap: () -> Unit,
) {
    entry<Home> {
        val viewModel: HomeViewModel = hiltViewModel()
        val viewState by viewModel.viewState.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            viewModel.singleEvent.collect { event ->
                when (event) {
                    is HomeEvent.ShowToast -> {
                        android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                    }

                    HomeEvent.NavigateMap -> onNavigateMap()
                }
            }
        }

        HomeScreen(
            viewState = viewState,
            onCheckServer = { viewModel.processIntent(HomeIntent.CheckServer) },
            onCheckDatabase = { viewModel.processIntent(HomeIntent.CheckDatabase) },
            onNavigateMap = { viewModel.processIntent(HomeIntent.NavigateToMap) }
        )

    }
}

fun EntryProviderScope<NavKey>.featureBSection(
    onSubRouteClick: () -> Unit,
) {
    entry<Map> {
        MapScreen()
    }

}

fun EntryProviderScope<NavKey>.featureCSection(
    onSubRouteClick: () -> Unit,
) {
    entry<com.houhackathon.greenmap_app.Notification> {

    }
}
