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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.houhackathon.greenmap_app.data.remote.dto.NewsDto
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

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

    Column(modifier = Modifier.fillMaxSize()) {
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

            NotificationTab.Server -> ServerFeedPlaceholder()
        }
    }
}

@Composable
private fun NotificationTabs(
    selected: NotificationTab,
    onSelect: (NotificationTab) -> Unit
) {
    val tabs = listOf(NotificationTab.News, NotificationTab.Server)
    TabRow(selectedTabIndex = tabs.indexOf(selected)) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                text = { Text(if (tab == NotificationTab.News) "Báo" else "Server") },
                selectedContentColor = MaterialTheme.colorScheme.primary,
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
    Box(modifier = Modifier.fillMaxSize()) {
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
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(news, key = { it.link ?: it.title ?: it.hashCode().toString() }) { item ->
                    NewsCard(item)
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
                Text(
                    text = listOfNotNull(news.source, news.publishedAt).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
private fun ServerFeedPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Tin tức từ server sẽ xuất hiện ở đây.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
