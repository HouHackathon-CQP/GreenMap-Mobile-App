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

package com.houhackathon.greenmap_app.domain.repository

import com.houhackathon.greenmap_app.data.remote.dto.WeatherForecastResponse
import com.houhackathon.greenmap_app.data.remote.dto.WeatherHanoiResponse
import com.houhackathon.greenmap_app.data.remote.dto.AqiHanoiResponse
import com.houhackathon.greenmap_app.extension.flow.Result

interface WeatherRepository {
    suspend fun getForecast(lat: Double, lon: Double): Result<WeatherForecastResponse>
    suspend fun getHanoiWeather(limit: Int = 100): Result<WeatherHanoiResponse>
    suspend fun getHanoiAqi(limit: Int = 100): Result<AqiHanoiResponse>
}
