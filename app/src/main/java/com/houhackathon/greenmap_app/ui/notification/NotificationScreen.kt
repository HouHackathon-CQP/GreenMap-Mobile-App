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

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.houhackathon.greenmap_app.data.remote.dto.NewsDto
import com.houhackathon.greenmap_app.domain.model.ServerNotification
import com.houhackathon.greenmap_app.ui.theme.Leaf100
import com.houhackathon.greenmap_app.ui.theme.Leaf200
import com.houhackathon.greenmap_app.ui.theme.Leaf700
import com.houhackathon.greenmap_app.ui.theme.Sand
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen() {
    val viewModel: NotificationViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.singleEvent.collect { event ->
            when (event) {
                is NotificationEvent.ShowToast -> android.widget.Toast
                    .makeText(context, event.message, android.widget.Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    val selectedNotification = viewState.selectedNotification
    if (selectedNotification != null) {
        NotificationDetailScreen(
            notification = selectedNotification,
            onBack = { viewModel.processIntent(NotificationIntent.DismissNotificationDetail) }
        )
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Sand
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Header()
                NotificationTabs(
                    selected = viewState.selectedTab,
                    onSelect = { viewModel.processIntent(NotificationIntent.SelectTab(it)) }
                )
                when (viewState.selectedTab) {
                    NotificationTab.News -> NewsList(
                        news = viewState.news,
                        isLoading = viewState.isLoading,
                        error = viewState.error,
                        onRefresh = { viewModel.processIntent(NotificationIntent.LoadNews) }
                    )
                    NotificationTab.Server -> ServerNotificationList(
                        notifications = viewState.serverNotifications,
                        onClear = { viewModel.processIntent(NotificationIntent.ClearServerNotifications) },
                        onNotificationClick = { viewModel.processIntent(NotificationIntent.ShowNotificationDetail(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Leaf100, Leaf200.copy(alpha = 0.9f), Color.White.copy(alpha = 0.85f))
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Green Pulse",
                style = MaterialTheme.typography.titleLarge,
                color = Leaf700
            )
            Text(
                text = "Tin tức, cảnh báo và cập nhật từ Green Map",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationTabs(
    selected: NotificationTab,
    onSelect: (NotificationTab) -> Unit
) {
    val tabs = listOf(NotificationTab.News, NotificationTab.Server)
    TabRow(
        selectedTabIndex = tabs.indexOf(selected),
        containerColor = Color.Transparent,
        contentColor = Leaf700,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier
                    .tabIndicatorOffset(tabPositions[tabs.indexOf(selected)])
                    .padding(horizontal = 32.dp),
                color = Leaf700
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                text = { Text(if (tab == NotificationTab.News) "Tin tức xanh" else "Thông báo") },
                selectedContentColor = Leaf700,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NewsList(
    news: List<NewsDto>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp)),
        color = Color.White.copy(alpha = 0.82f),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tin tức xanh",
                    style = MaterialTheme.typography.titleMedium,
                    color = Leaf700
                )
                TextButton(onClick = onRefresh, contentPadding = PaddingValues(horizontal = 10.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Leaf700, modifier = Modifier.height(18.dp))
                    Text(text = "Tải lại", color = Leaf700, modifier = Modifier.padding(start = 6.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 6.dp)
            ) {
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    error != null -> Text(
                        text = error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )

                    else -> LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(news, key = { it.link ?: it.title ?: it.hashCode().toString() }) { item ->
                            NewsCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewsDto) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !news.link.isNullOrBlank()) {
                news.link?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    context.startActivity(intent)
                }
            }
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (!news.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = news.imageUrl,
                    contentDescription = news.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = news.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!news.publishedAt.isNullOrBlank() || !news.source.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    news.source?.takeIf { it.isNotBlank() }?.let {
                        InfoChip(text = it, icon = Icons.Filled.Notifications)
                    }
                    news.publishedAt?.takeIf { it.isNotBlank() }?.let {
                        InfoChip(text = it, icon = Icons.Filled.DateRange)
                    }
                }
            }
            if (!news.description.isNullOrBlank()) {
                Text(
                    text = news.description.orEmpty().replace(Regex("<.*?>"), "").trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, icon: ImageVector? = null) {
    Surface(
        color = Leaf100,
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Leaf700,
                    modifier = Modifier.height(16.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Leaf700
            )
        }
    }
}

@Composable
private fun ServerNotificationList(
    notifications: List<ServerNotification>,
    onClear: () -> Unit,
    onNotificationClick: (ServerNotification) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp)),
        color = Color.White.copy(alpha = 0.82f),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
//                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Chưa có thông báo từ server",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Khi server đẩy cảnh báo, ứng dụng sẽ hiển thị ở đây và ngoài hệ thống.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thông báo từ server",
                        style = MaterialTheme.typography.titleMedium,
                        color = Leaf700
                    )
                    TextButton(onClick = onClear, contentPadding = PaddingValues(horizontal = 10.dp)) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_delete),
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.height(18.dp)
                        )
                        Text(text = "Xóa tất cả", color = Color.Red, modifier = Modifier.padding(start = 6.dp))
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        ServerNotificationCard(
                            notification = item,
                            onClick = { onNotificationClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerNotificationCard(
    notification: ServerNotification,
    onClick: () -> Unit,
) {
    val receivedAt = remember(notification.receivedAt) {
        val formatter = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        formatter.format(Date(notification.receivedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            InfoChip(text = "Server", icon = Icons.Filled.Notifications)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receivedAt,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = Leaf100,
                    shape = CircleShape,
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_dialog_info),
                            contentDescription = null,
                            tint = Leaf700,
                            modifier = Modifier.height(16.dp)
                        )
                        Text(
                            text = "Alert",
                            style = MaterialTheme.typography.labelMedium,
                            color = Leaf700
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationDetailScreen(
    notification: ServerNotification,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val receivedAt = remember(notification.receivedAt) {
        val formatter = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        formatter.format(Date(notification.receivedAt))
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chi tiết thông báo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyLarge
            )
            if (!notification.deeplink.isNullOrBlank()) {
                Text(
                    text = "Link: ${notification.deeplink}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Nhận lúc: $receivedAt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Đóng")
            }
        }
    }
}
