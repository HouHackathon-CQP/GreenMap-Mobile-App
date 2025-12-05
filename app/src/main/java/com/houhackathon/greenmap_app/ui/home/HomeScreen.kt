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

package com.houhackathon.greenmap_app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.ui.components.GreenButton
import com.houhackathon.greenmap_app.ui.components.GreenGhostButton

@Composable
fun HomeScreen(
    viewState: HomeViewState,
    onCheckServer: () -> Unit,
    onCheckDatabase: () -> Unit,
    onNavigateMap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F9D58),
                        Color(0xFF1EB980)
                    )
                )
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            color = Color.Transparent
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderSection()

                StatusCard(
                    title = stringResource(id = R.string.server_title),
                    status = viewState.serverStatus,
                    isLoading = viewState.isServerLoading,
                    isError = viewState.isServerError,
                    onAction = onCheckServer,
                    actionLabel = "Check server"
                )

                StatusCard(
                    title = stringResource(id = R.string.database_title),
                    status = viewState.databaseStatus,
                    isLoading = viewState.isDatabaseLoading,
                    isError = viewState.isDatabaseError,
                    onAction = onCheckDatabase,
                    actionLabel = "Check database"
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GreenGhostButton(
                        text = stringResource(id = R.string.btn_view_map),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateMap
                    )
                    GreenButton(
                        text = stringResource(id = R.string.btn_refresh),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCheckServer()
                            onCheckDatabase()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.home_title),
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(id = R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
        )
    }
}

@Composable
private fun StatusCard(
    title: String,
    status: String?,
    isLoading: Boolean,
    isError: Boolean,
    onAction: () -> Unit,
    actionLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusIndicator(isLoading = isLoading, isError = isError)
            }

            Text(
                text = status ?: stringResource(id = com.houhackathon.greenmap_app.R.string.status_not_checked),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isLoading -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> Color(0xFF1B5E20)
                }
            )

            GreenButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusIndicator(isLoading: Boolean, isError: Boolean) {
    val tint = when {
        isLoading -> MaterialTheme.colorScheme.primary
        isError -> MaterialTheme.colorScheme.error
        else -> Color(0xFF1B5E20)
    }
    Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(color = tint)
    }
}
