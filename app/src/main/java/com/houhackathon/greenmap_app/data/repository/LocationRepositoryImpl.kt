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
import com.houhackathon.greenmap_app.data.remote.dto.LocationDto
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.domain.repository.LocationRepository
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : LocationRepository {
    override suspend fun getLocationsByType(type: LocationType): Result<List<LocationDto>> {
        // API caps limit at 1000, so fetch with skip-based pagination.
        val pageLimit = 1000
        val allLocations = mutableListOf<LocationDto>()
        var skip = 0

        while (true) {
            when (val result = remoteDataSource.getLocations(type.queryName, pageLimit, skip)) {
                is Result.Success -> {
                    val data = result.data
                    allLocations.addAll(data)
                    if (data.size < pageLimit) {
                        return Result.Success(allLocations)
                    }
                    skip += pageLimit
                }
                is Result.Error -> return result
                is Result.Loading -> return result
            }
        }
    }
}
