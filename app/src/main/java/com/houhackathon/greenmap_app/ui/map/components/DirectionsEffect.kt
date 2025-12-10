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

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.houhackathon.greenmap_app.domain.model.DirectionPlan
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.ui.map.DirectionStore
import com.houhackathon.greenmap_app.ui.map.MarkerInfo
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap

@Composable
fun DirectionOverlayEffect(
    directionPlan: DirectionPlan?,
    mapLibreMap: MapLibreMap?,
    store: DirectionStore,
    markerInfoMap: MutableMap<Marker, MarkerInfo>,
    poiIconProvider: (LocationType) -> Icon?,
    refreshKey: Int,
) {
    LaunchedEffect(directionPlan, mapLibreMap, refreshKey) {
        val map = mapLibreMap ?: return@LaunchedEffect
        store.clear(map, markerInfoMap)
        val plan = directionPlan ?: return@LaunchedEffect
        val coordinates = plan.route.coordinates
        if (coordinates.isEmpty()) return@LaunchedEffect

        val latLngs = coordinates.map { LatLng(it.lat, it.lon) }
        store.routeLine = map.addPolyline(
            PolylineOptions()
                .addAll(latLngs)
                .color(Color.parseColor("#0D8F5A"))
                .width(6f)
        )

        store.startMarker = map.addMarker(
            MarkerOptions()
                .position(LatLng(plan.start.lat, plan.start.lon))
                .title(plan.start.name ?: "Điểm xuất phát")
                .snippet("Start")
        )
        store.startMarker?.let {
            markerInfoMap[it] = MarkerInfo(
                title = plan.start.name ?: "Điểm xuất phát",
                category = "Chỉ đường",
                lat = plan.start.lat,
                lon = plan.start.lon
            )
        }

        store.destinationMarker = map.addMarker(
            MarkerOptions()
                .position(LatLng(plan.destination.lat, plan.destination.lon))
                .title(plan.destination.name ?: "Điểm đến")
                .snippet("Destination")
        )
        store.destinationMarker?.let {
            markerInfoMap[it] = MarkerInfo(
                title = plan.destination.name ?: "Điểm đến",
                category = "Chỉ đường",
                lat = plan.destination.lat,
                lon = plan.destination.lon
            )
        }

        plan.viaPois.forEach { poi ->
            val icon = poi.type?.let { typeName ->
                LocationType.fromRaw(typeName)?.let { poiIconProvider(it) }
            }
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(poi.lat, poi.lon))
                    .title(poi.name ?: poi.type ?: "Điểm dừng")
                    .snippet("Via")
                    .icon(icon)
            )
            store.viaMarkers.add(marker)
            markerInfoMap[marker] = MarkerInfo(
                title = poi.name ?: poi.type ?: "Điểm dừng",
                category = "Điểm dừng",
                lat = poi.lat,
                lon = poi.lon
            )
        }

        runCatching {
            val boundsBuilder = LatLngBounds.Builder()
            latLngs.forEach(boundsBuilder::include)
            boundsBuilder.include(LatLng(plan.start.lat, plan.start.lon))
            boundsBuilder.include(LatLng(plan.destination.lat, plan.destination.lon))
            plan.viaPois.forEach { boundsBuilder.include(LatLng(it.lat, it.lon)) }
            val bounds = boundsBuilder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        }
    }
}
