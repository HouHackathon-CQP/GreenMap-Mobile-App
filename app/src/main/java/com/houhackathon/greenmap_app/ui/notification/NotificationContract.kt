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

import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState
import com.houhackathon.greenmap_app.data.remote.dto.NewsDto
import com.houhackathon.greenmap_app.domain.model.ServerNotification

data class NotificationViewState(
    val isLoading: Boolean = false,
    val news: List<NewsDto> = emptyList(),
    val serverNotifications: List<ServerNotification> = emptyList(),
    val error: String? = null,
    val selectedNotification: ServerNotification? = null,
    val selectedTab: NotificationTab = NotificationTab.News,
) : MviViewState

enum class NotificationTab { News, Server }

sealed class NotificationIntent : MviIntent {
    data object LoadNews : NotificationIntent()
    data class SelectTab(val tab: NotificationTab) : NotificationIntent()
    data object ClearServerNotifications : NotificationIntent()
    data class ShowNotificationDetail(val notification: ServerNotification) : NotificationIntent()
    data object DismissNotificationDetail : NotificationIntent()
}

sealed class NotificationEvent : MviSingleEvent {
    data class ShowToast(val message: String) : NotificationEvent()
}
