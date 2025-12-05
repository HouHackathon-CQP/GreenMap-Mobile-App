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
