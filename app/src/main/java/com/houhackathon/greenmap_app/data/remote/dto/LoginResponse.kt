package com.houhackathon.greenmap_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("access_token")
    val token: String? = null,
    @SerialName("token_type")
    val bearer: String? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("role")
    val role: String? = null
)
