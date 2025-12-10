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

package com.houhackathon.greenmap_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("start")
    val start: DirectionPoint? = null,
    @SerializedName("destination")
    val destination: DirectionPoint? = null,
    @SerializedName("via_pois")
    val viaPois: List<DirectionPoi> = emptyList(),
    @SerializedName("route")
    val route: DirectionRoute? = null,
    @SerializedName("summary")
    val summary: String? = null,
)

data class DirectionPoint(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lon")
    val lon: Double? = null,
)

data class DirectionPoi(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lon")
    val lon: Double? = null,
    @SerializedName("type")
    val type: String? = null,
)

data class DirectionRoute(
    @SerializedName("distance")
    val distance: Double? = null,
    @SerializedName("duration")
    val duration: Double? = null,
    @SerializedName("geometry")
    val geometry: DirectionGeometry? = null,
)

data class DirectionGeometry(
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("coordinates")
    val coordinates: List<List<Double>>? = null,
)
