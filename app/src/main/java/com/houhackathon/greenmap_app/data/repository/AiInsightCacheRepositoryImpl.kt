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

package com.houhackathon.greenmap_app.data.repository

import com.houhackathon.greenmap_app.data.local.dao.AiInsightDao
import com.houhackathon.greenmap_app.data.local.entity.AiInsightEntity
import com.houhackathon.greenmap_app.domain.model.AiInsightCache
import com.houhackathon.greenmap_app.domain.model.AiProvider
import com.houhackathon.greenmap_app.domain.repository.AiInsightCacheRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiInsightCacheRepositoryImpl @Inject constructor(
    private val dao: AiInsightDao,
) : AiInsightCacheRepository {
    override suspend fun getFreshInsight(
        provider: AiProvider,
        lat: Double,
        lon: Double,
        maxAgeMillis: Long,
    ): AiInsightCache? {
        val entity = dao.getInsight(provider.queryName, lat.toDouble(), lon.toDouble()) ?: return null
        val now = System.currentTimeMillis()
        val isFresh = now - entity.updatedAt <= maxAgeMillis
        return if (isFresh) entity.toDomain(provider) else null
    }

    override suspend fun saveInsight(cache: AiInsightCache) {
        val entity = AiInsightEntity(
            provider = cache.provider.queryName,
            lat = cache.lat,
            lon = cache.lon,
            model = cache.model,
            analysis = cache.analysis,
            locationName = cache.locationName,
            updatedAt = cache.updatedAt
        )
        dao.upsertInsight(entity)
    }

    private fun AiInsightEntity.toDomain(providerEnum: AiProvider? = null): AiInsightCache? {
        val provider = providerEnum ?: AiProvider.fromRaw(provider) ?: return null
        return AiInsightCache(
            provider = provider,
            lat = lat,
            lon = lon,
            model = model,
            analysis = analysis,
            locationName = locationName,
            updatedAt = updatedAt
        )
    }
}
