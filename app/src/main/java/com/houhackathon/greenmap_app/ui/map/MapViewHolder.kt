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
