package com.houhackathon.greenmap_app.domain.repository

import com.houhackathon.greenmap_app.data.remote.dto.LoginResponse
import com.houhackathon.greenmap_app.extension.flow.Result

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
}
