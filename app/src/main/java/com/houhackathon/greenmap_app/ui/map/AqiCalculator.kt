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

import kotlin.math.roundToInt

data class VietnamAqiResult(
    val index: Int,
    val category: VietnamAqiCategory,
)

enum class VietnamAqiCategory(val label: String) {
    GOOD("Tốt"),
    MODERATE("Trung bình"),
    UNHEALTHY_FOR_SENSITIVE("Nhạy cảm cho da"),
    UNHEALTHY("Không tốt"),
    VERY_UNHEALTHY("Cực kỳ không tốt"),
    HAZARDOUS("Nguy hại"),
    VERY_HAZARDOUS("Cực kỳ nguy hại"),
}

private data class AqiBreakpoint(
    val cLow: Double,
    val cHigh: Double,
    val iLow: Int,
    val iHigh: Int,
    val category: VietnamAqiCategory,
)

private val vietnamPm25Breakpoints = listOf(
    AqiBreakpoint(0.0, 25.0, 0, 50, VietnamAqiCategory.GOOD),
    AqiBreakpoint(25.0, 50.0, 51, 100, VietnamAqiCategory.MODERATE),
    AqiBreakpoint(50.0, 75.0, 101, 150, VietnamAqiCategory.UNHEALTHY_FOR_SENSITIVE),
    AqiBreakpoint(75.0, 100.0, 151, 200, VietnamAqiCategory.UNHEALTHY),
    AqiBreakpoint(100.0, 150.0, 201, 300, VietnamAqiCategory.VERY_UNHEALTHY),
    AqiBreakpoint(150.0, 250.0, 301, 400, VietnamAqiCategory.HAZARDOUS),
    AqiBreakpoint(250.0, 500.0, 401, 500, VietnamAqiCategory.VERY_HAZARDOUS),
)

/**
 * Calculates VN AQI for PM2.5 using the national concentration breakpoints.
 * Returns null when no breakpoint is available (unlikely with current table).
 */
fun calculateVietnamPm25Aqi(pm25: Double): VietnamAqiResult? {
    val normalized = pm25.coerceAtLeast(0.0)
    val breakpoint = vietnamPm25Breakpoints.firstOrNull { normalized <= it.cHigh }
        ?: vietnamPm25Breakpoints.lastOrNull()
        ?: return null

    val range = breakpoint.cHigh - breakpoint.cLow
    val index = if (range == 0.0) {
        breakpoint.iHigh
    } else {
        val ratio = (normalized - breakpoint.cLow) / range
        (ratio * (breakpoint.iHigh - breakpoint.iLow) + breakpoint.iLow).roundToInt()
    }

    return VietnamAqiResult(
        index = index.coerceIn(breakpoint.iLow, breakpoint.iHigh),
        category = breakpoint.category
    )
}
