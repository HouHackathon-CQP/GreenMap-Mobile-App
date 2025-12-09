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

package com.houhackathon.greenmap_app.domain.repository

import com.houhackathon.greenmap_app.data.remote.dto.RegisterNotificationResponse
import com.houhackathon.greenmap_app.domain.model.ServerNotification
import com.houhackathon.greenmap_app.extension.flow.Result
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeServerNotifications(): Flow<List<ServerNotification>>
    suspend fun saveServerNotification(notification: ServerNotification): Result<Unit>
    suspend fun clearNotifications(): Result<Unit>
    suspend fun registerNotificationDevice(token: String, platform: String): Result<RegisterNotificationResponse>
}
