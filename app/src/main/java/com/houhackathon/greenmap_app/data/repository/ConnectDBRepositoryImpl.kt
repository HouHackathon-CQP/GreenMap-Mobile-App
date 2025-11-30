package com.houhackathon.greenmap_app.data.repository

import com.houhackathon.greenmap_app.data.remote.RemoteDataSource
import com.houhackathon.greenmap_app.data.remote.dto.ApiStatusResponse
import com.houhackathon.greenmap_app.domain.repository.ConnectDBRepository
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectDBRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : ConnectDBRepository {
    override suspend fun connectDb(): Result<ApiStatusResponse> = remoteDataSource.connectDatabase()

}