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
}
