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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.houhackathon.greenmap_app.R
import com.houhackathon.greenmap_app.data.remote.dto.CurrentWeather

@Composable
fun CurrentWeatherCard(
    current: CurrentWeather?,
    source: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.0f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF1EB980), Color(0xFF0F9D58))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(id = R.string.weather_current),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (!source.isNullOrBlank()) {
                        Text(
                            text = source,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                Text(
                    text = current?.temp?.let { "${it}°C" } ?: "--°C",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Text(
                    text = current?.desc ?: stringResource(id = R.string.weather_placeholder_desc),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.9f))
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WeatherStat(
                        iconRes = R.drawable.water_drop,
                        label = stringResource(id = R.string.weather_humidity),
                        value = current?.humidity?.let { "$it%" } ?: "--"
                    )
                    WeatherStat(
                        iconRes = R.drawable.air,
                        label = stringResource(id = R.string.weather_wind),
                        value = current?.windSpeed?.let { "${it} km/h" } ?: "--"
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherStat(
    iconRes: Int,
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
