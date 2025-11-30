package com.houhackathon.greenmap_app.ui.auth

import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState

data class LoginViewState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val fullName: String? = null,
    val token: String? = null,
) : MviViewState

sealed class LoginIntent : MviIntent {
    data class UpdateEmail(val value: String) : LoginIntent()
    data class UpdatePassword(val value: String) : LoginIntent()
    data object Submit : LoginIntent()
    data object ClearError : LoginIntent()
    data object CheckSession : LoginIntent()
    data object Logout : LoginIntent()
}

sealed class LoginEvent : MviSingleEvent {
    data class ShowToast(val message: String) : LoginEvent()
    data object LoginSuccess : LoginEvent()
}
