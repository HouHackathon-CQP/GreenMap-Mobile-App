package com.houhackathon.greenmap_app.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView


@Composable
fun MapLibreScreen() {
    val mapView = rememberMapView()

    DisposableEffect(Unit) {
        mapView.onStart()

        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    MapViewContainer(mapView = mapView)
}


@Composable
private fun MapViewContainer(mapView: MapView) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView }
    )
}


@Composable
private fun rememberMapView(): MapView {
    val context = LocalContext.current
    return remember {
        MapView(context).apply {
            getMapAsync { map ->
                // 1. Set style cho bản đồ từ Maptiler
                // TODO: Thay "YOUR_API_KEY" bằng API Key của bạn từ maptiler.com
                val apiKey = "Eei2rj5brLExeS52RXZY "
                val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"
                map.setStyle(styleUrl)

                // 2. Giới hạn bản đồ trong khu vực Việt Nam
                val vietnamBounds = LatLngBounds.Builder()
                    .include(LatLng(23.39, 109.46)) // Góc Đông Bắc
                    .include(LatLng(8.18, 102.14))  // Góc Tây Nam
                    .build()
                map.setLatLngBoundsForCameraTarget(vietnamBounds)

                // 3. Thiết lập vị trí camera ban đầu là Hà Nội
                val hanoiPosition = CameraPosition.Builder()
                    .target(LatLng(21.0285, 105.8542)) // Tọa độ Hà Nội
                    .zoom(12.0) // Mức zoom hợp lý để thấy thành phố
                    .build()
                map.cameraPosition = hanoiPosition
            }
        }
    }
}
