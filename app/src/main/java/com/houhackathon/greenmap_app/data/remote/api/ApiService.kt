package com.houhackathon.greenmap_app.data.remote.api

import com.houhackathon.greenmap_app.data.remote.dto.ApiStatusResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/")
    suspend fun healthCheck(): Response<ApiStatusResponse>

    @GET("test-db")
    suspend fun testConnectDb(): Response<ApiStatusResponse>
}
