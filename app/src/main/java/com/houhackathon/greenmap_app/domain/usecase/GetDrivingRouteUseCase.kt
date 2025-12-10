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

package com.houhackathon.greenmap_app.domain.usecase

import com.houhackathon.greenmap_app.domain.model.GeoPoint
import com.houhackathon.greenmap_app.domain.repository.RoutingRepository
import javax.inject.Inject

class GetDrivingRouteUseCase @Inject constructor(
    private val repository: RoutingRepository,
) {
    suspend operator fun invoke(start: GeoPoint, end: GeoPoint) = repository.getDrivingRoute(start, end)
}
