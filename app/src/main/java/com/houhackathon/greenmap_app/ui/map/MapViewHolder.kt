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
import org.maplibre.android.maps.MapView

object MapViewHolder {
    private var mapView: MapView? = null
    private var initialized: Boolean = false

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
        mapView?.onDestroy()
        mapView = null
        initialized = false
    }
}
