package com.houhackathon.greenmap_app.domain.usecase

import com.houhackathon.greenmap_app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(username: String, password: String) =
        repository.login(username, password)
}
