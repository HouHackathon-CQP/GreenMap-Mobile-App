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

package com.houhackathon.greenmap_app.ui.notification

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.domain.usecase.GetHanoimoiNewsUseCase
import com.houhackathon.greenmap_app.domain.usecase.ClearServerNotificationsUseCase
import com.houhackathon.greenmap_app.domain.usecase.ObserveServerNotificationsUseCase
import com.houhackathon.greenmap_app.domain.model.ServerNotification
import com.houhackathon.greenmap_app.extension.flow.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getHanoimoiNews: GetHanoimoiNewsUseCase,
    private val observeServerNotifications: ObserveServerNotificationsUseCase,
    private val clearServerNotifications: ClearServerNotificationsUseCase,
    @ApplicationContext private val appContext: Context,
) : BaseMviViewModel<NotificationIntent, NotificationViewState, NotificationEvent>() {

    private val _viewState = MutableStateFlow(NotificationViewState())
    override val viewState: StateFlow<NotificationViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch { intentSharedFlow.collect(::handleIntent) }
        viewModelScope.launch { observeServerNotifications().collect(::onNotificationsUpdated) }
        processIntent(NotificationIntent.LoadNews)
    }

    private fun handleIntent(intent: NotificationIntent) {
        when (intent) {
            NotificationIntent.LoadNews -> loadNews()
            is NotificationIntent.SelectTab -> _viewState.update { it.copy(selectedTab = intent.tab) }
            NotificationIntent.ClearServerNotifications -> clearNotifications()
            is NotificationIntent.ShowNotificationDetail -> _viewState.update { it.copy(selectedNotification = intent.notification) }
            NotificationIntent.DismissNotificationDetail -> _viewState.update { it.copy(selectedNotification = null) }
        }
    }

    private fun onNotificationsUpdated(notifications: List<ServerNotification>) {
        _viewState.update { state ->
            val selected = state.selectedNotification
            val validSelected = notifications.firstOrNull { it.id == selected?.id }
            state.copy(serverNotifications = notifications, selectedNotification = validSelected)
        }
    }

    private fun loadNews(limit: Int = 50) {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, error = null) }
            when (val result = getHanoimoiNews(limit)) {
                is Result.Success -> _viewState.update {
                    it.copy(isLoading = false, news = result.data)
                }

                is Result.Error -> {
                    val message = result.exception.message ?: appContext.getString(R.string.news_error_generic)
                    sendEvent(NotificationEvent.ShowToast(message))
                    _viewState.update { it.copy(isLoading = false, error = message) }
                }

                Result.Loading -> _viewState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun clearNotifications() {
        viewModelScope.launch {
            when (val result = clearServerNotifications()) {
                is Result.Error -> {
                    val message = result.exception.message ?: appContext.getString(R.string.news_error_generic)
                    sendEvent(NotificationEvent.ShowToast(message))
                }
                else -> Unit
            }
        }
    }
}
