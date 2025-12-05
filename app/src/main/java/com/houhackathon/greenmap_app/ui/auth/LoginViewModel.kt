/*
 * Copyright 2025 HouHackathon-CQP
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.houhackathon.greenmap_app.ui.auth

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.core.datastore.UserPreferences
import com.houhackathon.greenmap_app.core.datastore.UserInfo
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.domain.usecase.LoginUseCase
import com.houhackathon.greenmap_app.extension.flow.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val appContext: Context,
) : BaseMviViewModel<LoginIntent, LoginViewState, LoginEvent>() {

    private val _viewState = MutableStateFlow(LoginViewState())
    override val viewState: StateFlow<LoginViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            intentSharedFlow.collect(::handleIntent)
        }
        processIntent(LoginIntent.CheckSession)
    }

    private fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> _viewState.update { it.copy(email = intent.value) }
            is LoginIntent.UpdatePassword -> _viewState.update { it.copy(password = intent.value) }
            LoginIntent.Submit -> submitLogin()
            LoginIntent.ClearError -> _viewState.update { it.copy(error = null) }
            LoginIntent.CheckSession -> loadSession()
            LoginIntent.Logout -> logout()
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            val firstInfo = userPreferences.userInfoFlow.first()
            updateFromInfo(firstInfo)
            userPreferences.userInfoFlow.drop(1).collect { info ->
                updateFromInfo(info)
            }
        }
    }

    private fun submitLogin() {
        val username = _viewState.value.email.trim()
        val password = _viewState.value.password
        if (username.isBlank() || password.isBlank()) {
            sendEvent(LoginEvent.ShowToast(appContext.getString(R.string.toast_fill_info)))
            return
        }

        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(username, password)) {
                is Result.Success -> {
                    _viewState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            email = result.data.email ?: it.email,
                            fullName = result.data.fullName ?: it.fullName,
                            token = result.data.token ?: it.token,
                            error = null
                        )
                    }
                    userPreferences.saveUser(
                        email = result.data.email,
                        token = result.data.token,
                        fullName = result.data.fullName
                    )
                    sendEvent(LoginEvent.LoginSuccess)
                }
                is Result.Error -> {
                    _viewState.update { it.copy(isLoading = false, error = result.exception.message) }
                    sendEvent(
                        LoginEvent.ShowToast(
                            result.exception.message ?: appContext.getString(R.string.toast_login_fail)
                        )
                    )
                }
                Result.Loading -> _viewState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            userPreferences.clear()
            _viewState.update {
                it.copy(
                    isLoggedIn = false,
                    token = null,
                    fullName = null,
                    email = "",
                    password = "",
                )
            }
        }
    }

    private fun updateFromInfo(info: UserInfo) {
        val loggedIn = !info.token.isNullOrBlank()
        _viewState.update {
            it.copy(
                isLoggedIn = loggedIn,
                fullName = info.fullName,
                email = info.email?:"",
                token = info.token
            )
        }
    }
}
