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

package com.houhackathon.greenmap_app.data.repository

import com.houhackathon.greenmap_app.data.local.dao.NotificationDao
import com.houhackathon.greenmap_app.data.local.entity.ServerNotificationEntity
import com.houhackathon.greenmap_app.data.remote.RemoteDataSource
import com.houhackathon.greenmap_app.domain.model.ServerNotification
import com.houhackathon.greenmap_app.domain.repository.NotificationRepository
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao,
    private val remoteDataSource: RemoteDataSource,
) : NotificationRepository {

    override fun observeServerNotifications(): Flow<List<ServerNotification>> {
        return dao.observeNotifications().map { items -> items.map { it.toDomain() } }
    }

    override suspend fun saveServerNotification(notification: ServerNotification): Result<Unit> {
        return Result.safeCall { dao.upsert(notification.toEntity()) }
    }

    override suspend fun clearNotifications(): Result<Unit> {
        return Result.safeCall { dao.clearAll() }
    }

    override suspend fun registerNotificationDevice(
        token: String,
        platform: String,
    ) = remoteDataSource.registerNotificationDevice(token, platform)

    private fun ServerNotificationEntity.toDomain(): ServerNotification {
        return ServerNotification(
            id = id,
            title = title,
            body = body,
            deeplink = deeplink,
            imageUrl = imageUrl,
            receivedAt = receivedAt,
            source = source,
            rawPayload = rawPayload
        )
    }

    private fun ServerNotification.toEntity(): ServerNotificationEntity {
        return ServerNotificationEntity(
            id = id,
            title = title,
            body = body,
            deeplink = deeplink,
            imageUrl = imageUrl,
            receivedAt = receivedAt,
            source = source,
            rawPayload = rawPayload
        )
    }
}
