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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
import com.houhackathon.greenmap_app.data.remote.dto.DailyWeather

@Composable
fun DailyForecastList(
    items: List<DailyWeather>,
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
                text = stringResource(id = R.string.weather_daily),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
            Icon(
                painter = painterResource(id = R.drawable.calendar_month),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        if (items.isEmpty()) {
            Text(
                text = stringResource(id = R.string.weather_empty_daily),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEachIndexed { index, day ->
                    DailyRow(day)
                    if (index != items.lastIndex) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyWeather) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatDate(day.date),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = day.desc ?: "",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.thermostat),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = buildString {
                    append(day.tempMin?.let { "${it}°" } ?: "--")
                    append(" / ")
                    append(day.tempMax?.let { "${it}°" } ?: "--")
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return "--"
    return value.substringBefore('T', value)
}
