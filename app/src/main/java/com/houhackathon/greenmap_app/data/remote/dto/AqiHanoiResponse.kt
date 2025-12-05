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

data class AqiHanoiResponse(
    val source: String? = null,
    @SerializedName("limit_requested")
    val limitRequested: Int? = null,
    val count: Int? = null,
    val data: List<AqiStationDto> = emptyList(),
)

data class AqiStationDto(
    val id: String? = null,
    val type: String? = null,
    val location: AqiGeoProperty? = null,
    @SerializedName("https://smartdatamodels.org/dataModel.Environment/pm25")
    val pm25: AqiProperty? = null,
    @SerializedName("stationName")
    val stationName: AqiStringProperty? = null,
    @SerializedName("provider")
    val provider: AqiStringProperty? = null,
)

data class AqiGeoProperty(
    val value: AqiPoint? = null,
)

data class AqiPoint(
    val type: String? = null,
    val coordinates: List<Double> = emptyList(), // [lon, lat]
)

data class AqiProperty(
    val value: Double? = null,
    val unitCode: String? = null,
    val observedAt: String? = null,
)

data class AqiStringProperty(
    val value: String? = null,
)
