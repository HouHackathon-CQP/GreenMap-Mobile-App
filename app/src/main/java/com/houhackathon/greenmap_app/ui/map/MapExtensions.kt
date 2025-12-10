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

import com.houhackathon.greenmap_app.domain.model.DirectionPlan
import kotlin.math.ceil
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap

fun formatDistance(distanceMeters: Double?): String? =
    distanceMeters?.let {
        if (it >= 1000) "%.2f km".format(it / 1000.0) else "%.0f m".format(it)
    }

fun formatDuration(durationSeconds: Double?): String? =
    durationSeconds?.let {
        val minutes = ceil(it / 60.0).toInt()
        if (minutes >= 60) {
            val hours = minutes / 60
            val remainMinutes = minutes % 60
            if (remainMinutes == 0) "$hours giờ" else "$hours giờ $remainMinutes phút"
        } else {
            "$minutes phút"
        }
    }

fun formatLatLon(lat: Double, lon: Double): String = "%.5f, %.5f".format(lat, lon)

fun MapLibreMap.focusDirectionRoute(plan: DirectionPlan?) {
    val route = plan?.route ?: return
    val points = route.coordinates
    if (points.isEmpty()) return
    val boundsBuilder = LatLngBounds.Builder()
    points.forEach { boundsBuilder.include(LatLng(it.lat, it.lon)) }
    boundsBuilder.include(LatLng(plan.start.lat, plan.start.lon))
    boundsBuilder.include(LatLng(plan.destination.lat, plan.destination.lon))
    val bounds = boundsBuilder.build()
    animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
}
