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
import com.houhackathon.greenmap_app.data.remote.dto.OsrmRouteResponse
import com.houhackathon.greenmap_app.domain.model.DirectionRoute
import com.houhackathon.greenmap_app.domain.model.GeoPoint
import com.houhackathon.greenmap_app.domain.repository.RoutingRepository
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutingRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : RoutingRepository {
    override suspend fun getDrivingRoute(start: GeoPoint, end: GeoPoint): Result<DirectionRoute> {
        return when (val response = remoteDataSource.getOsrmRoute(start, end)) {
            is Result.Success -> mapResponse(response.data)
            is Result.Error -> Result.error(response.exception)
            Result.Loading -> Result.loading()
        }
    }

    private fun mapResponse(dto: OsrmRouteResponse): Result<DirectionRoute> {
        val route = dto.routes.firstOrNull()
            ?: return Result.error(IllegalStateException("Route not found"))
        val coordinates = route.geometry?.coordinates?.mapNotNull { coord ->
            if (coord.size < 2) return@mapNotNull null
            val lon = coord[0]
            val lat = coord[1]
            GeoPoint(lat = lat, lon = lon)
        }?.filter { it.lat.isFinite() && it.lon.isFinite() } ?: emptyList()
        if (coordinates.size < 2) return Result.error(IllegalStateException("Invalid route geometry"))
        return Result.success(
            DirectionRoute(
                coordinates = coordinates,
                distanceMeters = route.distance,
                durationSeconds = route.duration
            )
        )
    }
}
