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

package com.houhackathon.greenmap_app.core.network

import com.houhackathon.greenmap_app.BuildConfig

object NetworkConfig {
    private const val DEFAULT_BASE_URL = "https://example.com/"
    const val DEFAULT_TIMEOUT_SECONDS = 30L

    val baseUrl: String
        get() = normalizeBaseUrl(BuildConfig.API_BASE_URL.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL)

    private fun normalizeBaseUrl(url: String): String {
        var result = url
        if (result.contains("127.0.0.1") || result.contains("localhost")) {
            result = result
                .replace("127.0.0.1", "10.0.2.2")
                .replace("localhost", "10.0.2.2")
        }
        return if (result.endsWith("/")) result else "$result/"
    }
}
