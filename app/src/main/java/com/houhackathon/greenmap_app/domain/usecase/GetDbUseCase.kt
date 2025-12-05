package com.houhackathon.greenmap_app.domain.usecase

import com.houhackathon.greenmap_app.domain.repository.ConnectDBRepository
import javax.inject.Inject

class GetDbUseCase @Inject constructor(
    private val repository: ConnectDBRepository,
) {
    suspend operator fun invoke() = repository.connectDb()
}