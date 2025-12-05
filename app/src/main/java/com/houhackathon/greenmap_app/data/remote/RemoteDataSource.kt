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

package com.houhackathon.greenmap_app.data.remote

import com.houhackathon.greenmap_app.core.network.safeApiCall
import com.houhackathon.greenmap_app.data.remote.api.ApiService
import com.houhackathon.greenmap_app.data.remote.dto.ApiStatusResponse
import com.houhackathon.greenmap_app.data.remote.dto.LoginRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val apiService: ApiService,
) {

    suspend fun healthCheck() = safeApiCall { apiService.healthCheck() }

    suspend fun connectDatabase() = safeApiCall { apiService.testConnectDb() }

    suspend fun login(username: String, password: String) =
        safeApiCall { apiService.login(LoginRequest(username, password)) }

    suspend fun getWeatherForecast(lat: Double, lon: Double) =
        safeApiCall { apiService.getWeatherForecast(lat, lon) }
}
