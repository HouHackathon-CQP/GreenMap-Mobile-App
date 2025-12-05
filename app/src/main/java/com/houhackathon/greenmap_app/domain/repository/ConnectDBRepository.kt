package com.houhackathon.greenmap_app.domain.repository

import com.houhackathon.greenmap_app.data.remote.dto.ApiStatusResponse
import com.houhackathon.greenmap_app.extension.flow.Result

interface ConnectDBRepository {
    suspend fun connectDb(): Result<ApiStatusResponse>
}