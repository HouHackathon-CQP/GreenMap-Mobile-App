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

package com.houhackathon.greenmap_app.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.houhackathon.greenmap_app.extension.flow.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Result<Pair<Double, Double>> = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(Result.error(SecurityException("Location permission not granted")))
            return@suspendCancellableCoroutine
        }

        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(Result.success(location.latitude to location.longitude))
                } else {
                    requestFreshLocation(cont)
                }
            }
            .addOnFailureListener { error ->
                if (cont.isActive) cont.resume(Result.error(error))
            }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun requestFreshLocation(
        cont: CancellableContinuation<Result<Pair<Double, Double>>>
    ) {
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (!cont.isActive) return@addOnSuccessListener
                if (location != null) {
                    cont.resume(Result.success(location.latitude to location.longitude))
                } else {
                    cont.resume(Result.error(IllegalStateException("Cannot acquire current location")))
                }
            }
            .addOnFailureListener { error ->
                if (cont.isActive) cont.resume(Result.error(error))
            }
    }
}
