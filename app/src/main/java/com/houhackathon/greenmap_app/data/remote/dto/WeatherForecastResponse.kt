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

data class WeatherForecastResponse(
    val source: String? = null,
    val location: ForecastLocation? = null,
    val data: ForecastData? = null,
)

data class ForecastLocation(
    val lat: Double? = null,
    val lon: Double? = null,
)

data class ForecastData(
    val current: CurrentWeather? = null,
    @SerializedName("hourly_24h")
    val hourly: List<HourlyWeather> = emptyList(),
    @SerializedName("daily_7days")
    val daily: List<DailyWeather> = emptyList(),
)

data class CurrentWeather(
    val temp: Double? = null,
    val humidity: Int? = null,
    @SerializedName("wind_speed")
    val windSpeed: Double? = null,
    val desc: String? = null,
    val time: String? = null,
)

data class HourlyWeather(
    val time: String? = null,
    val temp: Double? = null,
    @SerializedName("rain_prob")
    val rainProb: Int? = null,
    val desc: String? = null,
)

data class DailyWeather(
    val date: String? = null,
    @SerializedName("temp_max")
    val tempMax: Double? = null,
    @SerializedName("temp_min")
    val tempMin: Double? = null,
    val desc: String? = null,
)
