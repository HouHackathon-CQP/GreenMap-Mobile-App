package com.houhackathon.greenmap_app.data.repository

import com.houhackathon.greenmap_app.data.remote.RemoteDataSource
import com.houhackathon.greenmap_app.data.remote.dto.ApiStatusResponse
import com.houhackathon.greenmap_app.domain.repository.HealthRepository
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : HealthRepository {
    override suspend fun healthCheck(): Result<ApiStatusResponse> = remoteDataSource.healthCheck()
    override suspend fun checkDatabase(): Result<ApiStatusResponse> = remoteDataSource.connectDatabase()
}
