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

package com.houhackathon.greenmap_app.ui.map

import com.houhackathon.greenmap_app.domain.model.LocationType

enum class MapLayer(val label: String, val locationType: LocationType?) {
    WEATHER("Thời tiết", null),
    AQI("AQI", null),
    BICYCLE_RENTAL("Xe đạp", LocationType.BICYCLE_RENTAL),
    PUBLIC_PARK("Công viên", LocationType.PUBLIC_PARK),
    TOURIST_ATTRACTION("Du lịch", LocationType.TOURIST_ATTRACTION),
    CHARGING_STATION("Trạm sạc", LocationType.CHARGING_STATION);
}
