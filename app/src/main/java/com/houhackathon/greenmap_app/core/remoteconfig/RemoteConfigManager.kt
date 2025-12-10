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

package com.houhackathon.greenmap_app.core.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.houhackathon.greenmap_app.extension.flow.Result
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper around Firebase Remote Config to fetch+activate values
 * and expose typed getters by key.
 */
class RemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    suspend fun fetchAndActivate(): Result<Boolean> {
        return Result.safeCall { remoteConfig.fetchAndActivate().await() }
    }

    fun getString(key: String): String = remoteConfig.getString(key)

    fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)

    fun getLong(key: String): Long = remoteConfig.getLong(key)

    fun getDouble(key: String): Double = remoteConfig.getDouble(key)
}
