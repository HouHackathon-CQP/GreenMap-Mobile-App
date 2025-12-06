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

package com.houhackathon.greenmap_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.houhackathon.greenmap_app.data.local.entity.AiInsightEntity

@Dao
interface AiInsightDao {
    @Query("SELECT * FROM ai_insights WHERE provider = :provider AND lat = :lat AND lon = :lon LIMIT 1")
    suspend fun getInsight(provider: String, lat: Double, lon: Double): AiInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInsight(entity: AiInsightEntity)
}
