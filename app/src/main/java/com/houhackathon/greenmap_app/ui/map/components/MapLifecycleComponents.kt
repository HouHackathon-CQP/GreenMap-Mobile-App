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

package com.houhackathon.greenmap_app.ui.map.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.houhackathon.greenmap_app.ui.map.MapEvent
import com.houhackathon.greenmap_app.ui.map.MapViewHolder
import com.houhackathon.greenmap_app.ui.map.MapViewModel
import com.houhackathon.greenmap_app.ui.map.enableMyLocation
import com.houhackathon.greenmap_app.ui.map.hasLocationPermission
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun MapEventHandler(viewModel: MapViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.singleEvent.collect { event ->
            when (event) {
                is MapEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}

@Composable
fun LocationPermissionEffect(
    hasLocationPermission: Boolean,
    mapView: MapView,
    onPermissionChanged: (Boolean) -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onPermissionChanged(granted)
        if (granted && MapViewHolder.isInitialized()) {
            mapView.getMapAsync { enableMyLocation(it, mapView) }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

@Composable
fun MapInitializer(
    mapView: MapView,
    hasLocationPermission: Boolean,
    onMapReady: (MapLibreMap) -> Unit,
    onMapPrepared: () -> Unit,
    onSetup: (MapLibreMap) -> Unit,
    onReuse: (MapLibreMap) -> Unit = {},
) {
    LaunchedEffect(mapView, hasLocationPermission) {
        if (!MapViewHolder.isInitialized()) {
            mapView.getMapAsync { map ->
                onSetup(map)
                MapViewHolder.markInitialized()
                onMapPrepared()
            }
        } else {
            if (hasLocationPermission) {
                mapView.getMapAsync {
                    enableMyLocation(it, mapView)
                    onReuse(it)
                }
            }
            mapView.getMapAsync {
                onMapReady(it)
                onReuse(it)
            }
            onMapPrepared()
        }
    }
}

@Composable
fun MapLifecycleHandler(mapView: MapView) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, mapView) {
        mapView.onStart()
        mapView.onResume()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                // Không destroy ở đây để giữ MapView khi đổi tab
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
        }
    }
}
