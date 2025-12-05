package com.houhackathon.greenmap_app.data.repository

import com.houhackathon.greenmap_app.data.remote.RemoteDataSource
import com.houhackathon.greenmap_app.data.remote.dto.LoginResponse
import com.houhackathon.greenmap_app.domain.repository.AuthRepository
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : AuthRepository {
    override suspend fun login(username: String, password: String): Result<LoginResponse> {
        return remoteDataSource.login(username, password)
    }
}
