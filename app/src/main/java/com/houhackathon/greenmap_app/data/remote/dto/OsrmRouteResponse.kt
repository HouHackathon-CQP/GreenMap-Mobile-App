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

data class OsrmRouteResponse(
    @SerializedName("routes")
    val routes: List<OsrmRoute> = emptyList()
)

data class OsrmRoute(
    @SerializedName("geometry")
    val geometry: OsrmGeometry? = null,
    @SerializedName("distance")
    val distance: Double? = null,
    @SerializedName("duration")
    val duration: Double? = null,
)

data class OsrmGeometry(
    @SerializedName("coordinates")
    val coordinates: List<List<Double>>? = null,
    @SerializedName("type")
    val type: String? = null,
)
