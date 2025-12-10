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

import com.houhackathon.greenmap_app.data.remote.RemoteDataSource
import com.houhackathon.greenmap_app.data.remote.dto.DEFAULT_DIRECTION_MODEL
import com.houhackathon.greenmap_app.data.remote.dto.DirectionsResponse
import com.houhackathon.greenmap_app.domain.model.DirectionLocation
import com.houhackathon.greenmap_app.domain.model.DirectionPlan
import com.houhackathon.greenmap_app.domain.model.DirectionRoute
import com.houhackathon.greenmap_app.domain.model.DirectionViaPoi
import com.houhackathon.greenmap_app.domain.model.GeoPoint
import com.houhackathon.greenmap_app.domain.repository.DirectionRepository
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectionRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : DirectionRepository {
    override suspend fun getDirections(
        question: String,
        currentLat: Double,
        currentLon: Double,
        model: String?,
    ): Result<DirectionPlan> {
        val resolvedModel = model?.takeIf { it.isNotBlank() } ?: DEFAULT_DIRECTION_MODEL
        return when (val response =
            remoteDataSource.getDirections(question, currentLat, currentLon, resolvedModel)) {
            is Result.Success -> mapDirections(response.data)
            is Result.Error -> Result.error(response.exception)
            Result.Loading -> Result.loading()
        }
    }

    private fun mapDirections(dto: DirectionsResponse): Result<DirectionPlan> {
        val startLat = dto.start?.lat
        val startLon = dto.start?.lon
        val destLat = dto.destination?.lat
        val destLon = dto.destination?.lon
        val geometryPoints = dto.route?.geometry?.coordinates
            ?.mapNotNull { coord ->
                if (coord.size < 2) return@mapNotNull null
                val lon = coord[0]
                val lat = coord[1]
                GeoPoint(lat = lat, lon = lon)
            }
            ?.filter { it.lat.isFinite() && it.lon.isFinite() }
            .orEmpty()

        if (startLat == null || startLon == null || destLat == null || destLon == null) {
            return Result.error(IllegalStateException("Missing start or destination"))
        }
        if (geometryPoints.isEmpty()) {
            return Result.error(IllegalStateException("Missing route geometry"))
        }

        val viaPois = dto.viaPois.mapNotNull { poi ->
            val lat = poi.lat
            val lon = poi.lon
            if (lat == null || lon == null) return@mapNotNull null
            DirectionViaPoi(
                name = poi.name,
                lat = lat,
                lon = lon,
                type = poi.type
            )
        }

        val plan = DirectionPlan(
            start = DirectionLocation(
                name = dto.start?.name,
                lat = startLat,
                lon = startLon,
            ),
            destination = DirectionLocation(
                name = dto.destination?.name,
                lat = destLat,
                lon = destLon,
            ),
            viaPois = viaPois,
            route = DirectionRoute(
                coordinates = geometryPoints,
                distanceMeters = dto.route?.distance,
                durationSeconds = dto.route?.duration
            ),
            summary = dto.summary
        )
        return Result.success(plan)
    }
}
