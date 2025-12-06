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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.data.remote.dto.HourlyWeather

@Composable
fun HourlyForecastRow(
    items: List<HourlyWeather>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.weather_hourly),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        if (items.isEmpty()) {
            Text(
                text = stringResource(id = R.string.weather_empty_hourly),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { hour ->
                    HourlyChip(hour)
                }
            }
        }
    }
}

@Composable
private fun HourlyChip(hour: HourlyWeather) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatHour(hour.time),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D7C4D)
                )
            )
            Icon(
                painter = painterResource(R.drawable.cloud),
                contentDescription = null,
                tint = Color(0xFF0F9D58),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = hour.temp?.let { "${it}°" } ?: "--",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D7C4D)
                )
            )
            Text(
                text = hour.desc ?: "",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF666666)
                )
            )
        }
    }
}

private fun formatHour(value: String?): String {
    if (value.isNullOrBlank()) return "--"
    val timePart = value.substringAfter('T', value)
    return if (timePart.length >= 5) timePart.take(5) else timePart
}
