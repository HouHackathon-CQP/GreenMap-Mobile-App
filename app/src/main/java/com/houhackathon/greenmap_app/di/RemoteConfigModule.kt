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

package com.houhackathon.greenmap_app.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.houhackathon.greenmap_app.BuildConfig
import com.houhackathon.greenmap_app.core.remoteconfig.RemoteConfigManager
import com.houhackathon.greenmap_app.core.remoteconfig.RemoteConfigKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {

    private val defaults: Map<String, Any> = mapOf(
        RemoteConfigKeys.DIRECTION_FLAG to false,
    )

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance().apply {
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
                .build()
            setConfigSettingsAsync(settings)
            if (defaults.isNotEmpty()) {
                setDefaultsAsync(defaults)
            }
        }
    }

    @Provides
    @Singleton
    fun provideRemoteConfigManager(
        remoteConfig: FirebaseRemoteConfig,
    ): RemoteConfigManager = RemoteConfigManager(remoteConfig)
}
