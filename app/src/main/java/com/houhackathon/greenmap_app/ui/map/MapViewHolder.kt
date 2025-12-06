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

import android.content.Context
import com.houhackathon.greenmap_app.domain.model.LocationType
import org.maplibre.android.annotations.Marker
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

object MapViewHolder {
    private var mapView: MapView? = null
    private var initialized: Boolean = false
    val markerStore: MarkerStore = MarkerStore()

    fun getOrCreate(context: Context): MapView {
        val current = mapView
        if (current != null) return current
        return MapView(context.applicationContext).apply {
            onCreate(null)
            mapView = this
        }
    }

    fun isInitialized(): Boolean = initialized

    fun markInitialized() {
        initialized = true
    }

    fun destroy() {
        mapView?.getMapAsync { markerStore.clear(it) }
        mapView?.onDestroy()
        mapView = null
        initialized = false
        markerStore.clear()
    }
}

class MarkerStore(
    val markerInfoMap: MutableMap<Marker, MarkerInfo> = mutableMapOf(),
    val weatherMarkers: MutableList<Marker> = mutableListOf(),
    val aqiMarkers: MutableList<Marker> = mutableListOf(),
    val poiMarkers: MutableMap<LocationType, MutableList<Marker>> = mutableMapOf(),
) {
    fun clear(mapLibreMap: MapLibreMap? = null) {
        val map = mapLibreMap
        if (map != null) {
            weatherMarkers.forEach(map::removeAnnotation)
            aqiMarkers.forEach(map::removeAnnotation)
            poiMarkers.values.flatten().forEach(map::removeAnnotation)
        }
        weatherMarkers.clear()
        aqiMarkers.clear()
        poiMarkers.clear()
        markerInfoMap.clear()
    }
}
