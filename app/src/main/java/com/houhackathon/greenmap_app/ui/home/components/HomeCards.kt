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

package com.houhackathon.greenmap_app.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.data.remote.dto.CurrentWeather
import com.houhackathon.greenmap_app.domain.model.AiProvider
import com.houhackathon.greenmap_app.ui.components.GreenButton
import com.houhackathon.greenmap_app.ui.components.GreenGhostButton

@Composable
fun LocationPill(
    text: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = Color.White
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Cập nhật theo vị trí của bạn",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onRefreshForecast: () -> Unit,
    onAnalyzeAi: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GreenGhostButton(
            text = stringResource(id = R.string.btn_refresh),
            onClick = onRefreshForecast,
            modifier = Modifier.weight(1f)
        )
        GreenButton(
            text = stringResource(id = R.string.ai_action_analyze),
            onClick = onAnalyzeAi,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AiInsightCard(
    provider: AiProvider,
    model: String?,
    analysis: String?,
    isLoading: Boolean,
    error: String?,
    onProviderSelected: (AiProvider) -> Unit,
    onAnalyzeClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102B44)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(id = R.string.ai_insight_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(id = R.string.ai_insight_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
                ModelBadge(model = model)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AiProvider.values().forEach { providerOption ->
                    ProviderChip(
                        provider = providerOption,
                        isSelected = providerOption == provider,
                        onClick = { onProviderSelected(providerOption) }
                    )
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.8f),
                    color = Color(0xFF1EB980)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF123E57), Color(0xFF0E2C44))
                        )
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = analysis?.takeIf { it.isNotBlank() }
                        ?: stringResource(id = if (isLoading) R.string.ai_loading else R.string.weather_placeholder_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                )
            }

            if (!error.isNullOrBlank()) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(id = R.string.ai_model_label)}: ${model ?: provider.label}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                GreenButton(
                    text = stringResource(id = R.string.ai_action_refresh),
                    onClick = onAnalyzeClick,
                    modifier = Modifier.weight(0.55f, fill = false),
                )
            }
        }
    }
}

@Composable
fun ProviderChip(
    provider: AiProvider,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1EB980).copy(alpha = 0.18f) else Color.White.copy(
                alpha = 0.08f
            ),
        ),
        border = if (isSelected) BorderStroke(1.2.dp, Color(0xFF1EB980)) else BorderStroke(
            1.2.dp,
            Color.White.copy(alpha = 0.15f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isSelected) Color(0xFF1EB980) else Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            Text(
                text = when (provider) {
                    AiProvider.GEMINI -> stringResource(id = R.string.ai_provider_gemini)
                    AiProvider.GROQ -> stringResource(id = R.string.ai_provider_grog)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun ModelBadge(model: String?) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Text(
            text = "${stringResource(id = R.string.ai_model_label)}: ${model ?: "--"}",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
        )
    }
}

@Composable
fun CurrentWeatherLarge(
    current: CurrentWeather?,
    onNavigateMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102840)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lúc này",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                    Text(
                        text = current?.temp?.let { "${it}°" } ?: "--°",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = current?.desc ?: "Đang tải...",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    Text(
                        text = current?.temp?.let { "Cảm giác như ${it}°" } ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2236)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Độ ẩm: ${current?.humidity ?: "--"}%",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Gió: ${current?.windSpeed ?: "--"} km/h",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    GreenGhostButton(
                        text = "Mở bản đồ",
                        onClick = onNavigateMap,
                        modifier = Modifier
                            .weight(0.6f, fill = false),
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastCardContainer(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2236)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
