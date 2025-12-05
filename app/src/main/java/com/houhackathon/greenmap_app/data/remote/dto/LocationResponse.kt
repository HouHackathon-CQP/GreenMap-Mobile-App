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

data class LocationDto(
    @SerializedName("@context")
    val context: String? = null,
    val id: String? = null,
    val type: String? = null,
    val location: LocationPointDto? = null,
    val description: String? = null,
    val name: String? = null,
    @SerializedName("data_source")
    val dataSource: String? = null,
    @SerializedName("db_id")
    val dbId: Int? = null,
    @SerializedName("is_editable")
    val isEditable: Boolean? = null,
)

data class LocationPointDto(
    val type: String? = null,
    val coordinates: List<Double> = emptyList(), // [lon, lat]
)
