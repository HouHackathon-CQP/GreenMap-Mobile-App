package com.houhackathon.greenmap_app.data.remote.api

import com.houhackathon.greenmap_app.data.remote.dto.ApiStatusResponse
import com.houhackathon.greenmap_app.data.remote.dto.LoginRequest
import com.houhackathon.greenmap_app.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/")
    suspend fun healthCheck(): Response<ApiStatusResponse>

    @GET("test-db")
    suspend fun testConnectDb(): Response<ApiStatusResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}
