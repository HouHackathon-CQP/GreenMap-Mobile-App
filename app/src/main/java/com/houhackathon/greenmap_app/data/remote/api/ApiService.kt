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

package com.houhackathon.greenmap_app.data.remote.api

import com.houhackathon.greenmap_app.data.remote.dto.ApiStatusResponse
import com.houhackathon.greenmap_app.data.remote.dto.LoginRequest
import com.houhackathon.greenmap_app.data.remote.dto.LoginResponse
import com.houhackathon.greenmap_app.data.remote.dto.WeatherForecastResponse
import com.houhackathon.greenmap_app.data.remote.dto.WeatherHanoiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("/")
    suspend fun healthCheck(): Response<ApiStatusResponse>

    @GET("test-db")
    suspend fun testConnectDb(): Response<ApiStatusResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("weather/forecast")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): Response<WeatherForecastResponse>

    @GET("weather/hanoi")
    suspend fun getHanoiWeather(
        @Query("limit") limit: Int = 100,
    ): Response<WeatherHanoiResponse>
}
