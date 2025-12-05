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

data class WeatherHanoiResponse(
    val source: String? = null,
    val count: Int? = null,
    val data: List<WeatherStationDto> = emptyList(),
)

data class WeatherStationDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("https://smartdatamodels.org/address")
    val address: WeatherAddressDto? = null,
    val location: WeatherPoint? = null,
    @SerializedName("https://smartdatamodels.org/dataModel.Environment/temperature")
    val temperature: Double? = null,
    @SerializedName("https://smartdatamodels.org/dataModel.Environment/relativeHumidity")
    val relativeHumidity: Double? = null,
    @SerializedName("https://smartdatamodels.org/dataModel.Environment/weatherType")
    val weatherType: String? = null,
    @SerializedName("https://smartdatamodels.org/dataModel.Environment/windSpeed")
    val windSpeed: Double? = null,
    @SerializedName("https://smartdatamodels.org/dateObserved")
    val dateObserved: String? = null,
)

data class WeatherAddressDto(
    val addressLocality: String? = null,
    val addressRegion: String? = null,
    val addressCountry: String? = null,
)

data class WeatherPoint(
    val type: String? = null,
    val coordinates: List<Double> = emptyList(), // [lon, lat]
)
