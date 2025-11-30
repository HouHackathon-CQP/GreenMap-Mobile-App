package com.houhackathon.greenmap_app.domain.usecase

import com.houhackathon.greenmap_app.domain.repository.HealthRepository
import javax.inject.Inject

class GetHealthStatusUseCase @Inject constructor(
    private val repository: HealthRepository,
) {
    suspend operator fun invoke() = repository.healthCheck()
}
