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

package com.houhackathon.greenmap_app.domain.model

data class DirectionPlan(
    val start: DirectionLocation,
    val destination: DirectionLocation,
    val viaPois: List<DirectionViaPoi>,
    val route: DirectionRoute,
    val summary: String?,
)

data class DirectionLocation(
    val name: String?,
    val lat: Double,
    val lon: Double,
)

fun DirectionLocation.toGeoPoint(): GeoPoint = GeoPoint(lat = lat, lon = lon)

data class DirectionViaPoi(
    val name: String?,
    val lat: Double,
    val lon: Double,
    val type: String?,
)

data class DirectionRoute(
    val coordinates: List<GeoPoint>,
    val distanceMeters: Double?,
    val durationSeconds: Double?,
)

data class GeoPoint(
    val lat: Double,
    val lon: Double,
)
