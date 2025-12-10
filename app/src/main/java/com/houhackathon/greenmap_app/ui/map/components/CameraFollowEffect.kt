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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.houhackathon.greenmap_app.domain.model.GeoPoint
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

@Composable
fun CameraFollowEffect(
    mapLibreMap: MapLibreMap?,
    location: GeoPoint?,
    bearing: Float?,
    enabled: Boolean,
    navigationMode: Boolean = false,
    tiltDegrees: Double = 50.0,
) {
    LaunchedEffect(mapLibreMap, location, bearing, enabled) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val point = location ?: return@LaunchedEffect
        if (!enabled) return@LaunchedEffect
        val currentZoom = map.cameraPosition.zoom.takeIf { it >= 15.0 } ?: 16.0
        val resolvedBearing = bearing?.toDouble() ?: map.cameraPosition.bearing
        val cameraPosition = CameraPosition.Builder(map.cameraPosition)
            .target(LatLng(point.lat, point.lon))
            .zoom(currentZoom)
            .bearing(resolvedBearing)
            .tilt(if (navigationMode) tiltDegrees else map.cameraPosition.tilt)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
}
