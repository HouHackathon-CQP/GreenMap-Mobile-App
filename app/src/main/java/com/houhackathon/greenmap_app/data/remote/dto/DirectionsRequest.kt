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

package com.houhackathon.greenmap_app.data.remote.dto

import com.google.gson.annotations.SerializedName

const val DEFAULT_DIRECTION_MODEL = "openai/gpt-oss-20b"

data class DirectionsRequest(
    @SerializedName("question")
    val question: String,
    @SerializedName("current_lat")
    val currentLat: Double,
    @SerializedName("current_lon")
    val currentLon: Double,
    @SerializedName("model")
    val model: String = DEFAULT_DIRECTION_MODEL,
)
