package com.houhackathon.greenmap_app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.houhackathon.greenmap_app.BuildConfig
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapViewHolder.getOrCreate(context) }
    var isMapReady by remember { mutableStateOf(MapViewHolder.isInitialized()) }

    LaunchedEffect(mapView) {
        if (!MapViewHolder.isInitialized()) {
            mapView.getMapAsync { map ->
                setupMap(map, mapView)
                MapViewHolder.markInitialized()
                isMapReady = true
            }
        } else {
            isMapReady = true
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        // khi màn Map được hiển thị lại sau khi đổi tab
        mapView.onStart()
        mapView.onResume()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> MapViewHolder.destroy()
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

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { /* giữ nguyên để tránh recreate khi recompose */ }
        )

        if (!isMapReady) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

private fun setupMap(map: MapLibreMap, mapView: MapView) {
    val apiKey = BuildConfig.MAPTILER_API_KEY
    val styleUrl = buildStyleUrl(apiKey)

    map.setStyle(styleUrl) { _ ->
        enableMyLocation(map, mapView)
    }

    val vietnamBounds = LatLngBounds.Builder()
        .include(LatLng(23.39, 109.46))
        .include(LatLng(8.18, 102.14))
        .build()
    map.setLatLngBoundsForCameraTarget(vietnamBounds)

    val hanoiPosition = CameraPosition.Builder()
        .target(LatLng(21.0285, 105.8542))
        .zoom(12.0)
        .build()
    map.cameraPosition = hanoiPosition

    map.uiSettings.apply {
        isRotateGesturesEnabled = true
        isTiltGesturesEnabled = true
        isZoomGesturesEnabled = true
        isScrollGesturesEnabled = true
    }
}

private fun enableMyLocation(map: MapLibreMap, mapView: MapView) {
    val context = mapView.context
    val hasLocationPermission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    if (!hasLocationPermission) return

    val style = map.style ?: return
    val activationOptions = LocationComponentActivationOptions.builder(context, style)
        .useDefaultLocationEngine(true)
        .build()

    map.locationComponent.apply {
        activateLocationComponent(activationOptions)
        isLocationComponentEnabled = true
        cameraMode = CameraMode.TRACKING
        renderMode = RenderMode.COMPASS
    }
}

private const val MAPLIBRE_DEMO_STYLE = "https://demotiles.maplibre.org/style.json"

private fun buildStyleUrl(apiKey: String): String =
    if (apiKey.isBlank()) MAPLIBRE_DEMO_STYLE
    else "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"
