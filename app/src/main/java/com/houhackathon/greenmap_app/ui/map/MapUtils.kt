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

package com.houhackathon.greenmap_app.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.houhackathon.greenmap_app.BuildConfig
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.ui.theme.BikeAmber
import com.houhackathon.greenmap_app.ui.theme.ChargeMint
import com.houhackathon.greenmap_app.ui.theme.ParkGreen
import com.houhackathon.greenmap_app.ui.theme.TourismCoral
import org.maplibre.android.annotations.Icon
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.camera.CameraPosition
import toMapLibreIcon

private const val MAPLIBRE_DEMO_STYLE = "https://demotiles.maplibre.org/style.json"

fun setupMap(
    map: MapLibreMap,
    mapView: MapView,
    hasLocationPermission: Boolean,
    onStyleReady: () -> Unit = {}
) {
    val apiKey = BuildConfig.MAPTILER_API_KEY
    val styleUrl = buildStyleUrl(apiKey)

    map.setStyle(styleUrl) { _ ->
        if (hasLocationPermission) {
            enableMyLocation(map, mapView)
        }
        onStyleReady()
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

fun enableMyLocation(map: MapLibreMap, mapView: MapView) {
    val context = mapView.context
    if (!hasLocationPermission(context)) return

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

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

private fun buildStyleUrl(apiKey: String): String =
    if (apiKey.isBlank()) MAPLIBRE_DEMO_STYLE
    else "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"

fun buildPoiIconMap(context: Context): Map<LocationType, Icon> = mapOf(
    LocationType.PUBLIC_PARK to context.toMapLibreIcon(R.drawable.ic_park, ParkGreen),
    LocationType.CHARGING_STATION to context.toMapLibreIcon(R.drawable.ic_ev, ChargeMint),
    LocationType.BICYCLE_RENTAL to context.toMapLibreIcon(R.drawable.ic_bike, BikeAmber),
    LocationType.TOURIST_ATTRACTION to context.toMapLibreIcon(R.drawable.ic_tour, TourismCoral),
)
