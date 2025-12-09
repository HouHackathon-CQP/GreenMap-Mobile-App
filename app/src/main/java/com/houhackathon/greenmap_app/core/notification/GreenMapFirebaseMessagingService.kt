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

package com.houhackathon.greenmap_app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.houhackathon.greenmap_app.MainActivity
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.domain.model.ServerNotification
import com.houhackathon.greenmap_app.domain.usecase.RegisterNotificationDeviceUseCase
import com.houhackathon.greenmap_app.domain.usecase.SaveServerNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

@AndroidEntryPoint
class GreenMapFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var saveServerNotification: SaveServerNotificationUseCase

    @Inject
    lateinit var registerNotificationDevice: RegisterNotificationDeviceUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.tag("GreenMapFCM").i("New FCM token: $token")
        serviceScope.launch {
            registerNotificationDevice(token, NotificationConstants.PLATFORM_ANDROID)
                .onSuccess { Timber.tag("GreenMapFCM").i("Registered device for server notifications, id=${it.id}") }
                .onError { Timber.tag("GreenMapFCM").e(it, "Failed to register FCM device token") }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notification = message.toServerNotification() ?: return

        serviceScope.launch {
            saveServerNotification(notification)
        }
        showSystemNotification(notification)
    }

    private fun showSystemNotification(notification: ServerNotification) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannelIfNeeded(notificationManager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationConstants.EXTRA_TARGET_ROUTE, NotificationConstants.TARGET_ROUTE_NOTIFICATION)
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, notification.id)
            notification.deeplink?.let { putExtra(NotificationConstants.EXTRA_DEEPLINK, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NotificationConstants.CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(notification.title.ifBlank { getString(R.string.app_name) })
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(this)
            .notify(notification.id.hashCode(), builder.build())
    }

    private fun createChannelIfNeeded(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationConstants.CHANNEL_ID_ALERTS,
                NotificationConstants.CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESCRIPTION_ALERTS
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun RemoteMessage.toServerNotification(): ServerNotification? {
        val title = data["title"] ?: notification?.title ?: return null
        val body = data["body"] ?: notification?.body ?: ""
        val deeplink = data["deeplink"] ?: notification?.link?.toString()
        val imageUrl = data["imageUrl"] ?: notification?.imageUrl?.toString()
        val rawPayload = if (data.isNotEmpty()) {
            data.entries.joinToString(prefix = "{", postfix = "}") { "\"${it.key}\":\"${it.value}\"" }
        } else {
            null
        }

        return ServerNotification(
            id = data["id"] ?: messageId ?: UUID.randomUUID().toString(),
            title = title,
            body = body,
            deeplink = deeplink,
            imageUrl = imageUrl,
            receivedAt = System.currentTimeMillis(),
            source = from,
            rawPayload = rawPayload
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
