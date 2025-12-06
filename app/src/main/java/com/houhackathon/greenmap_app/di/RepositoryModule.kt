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

import com.houhackathon.greenmap_app.data.repository.HealthRepositoryImpl
import com.houhackathon.greenmap_app.data.repository.AuthRepositoryImpl
import com.houhackathon.greenmap_app.data.repository.WeatherRepositoryImpl
import com.houhackathon.greenmap_app.data.repository.LocationRepositoryImpl
import com.houhackathon.greenmap_app.data.repository.NewsRepositoryImpl
import com.houhackathon.greenmap_app.data.repository.AiInsightCacheRepositoryImpl
import com.houhackathon.greenmap_app.domain.repository.HealthRepository
import com.houhackathon.greenmap_app.domain.repository.AuthRepository
import com.houhackathon.greenmap_app.domain.repository.WeatherRepository
import com.houhackathon.greenmap_app.domain.repository.LocationRepository
import com.houhackathon.greenmap_app.domain.repository.NewsRepository
import com.houhackathon.greenmap_app.domain.repository.AiInsightCacheRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindNewsRepository(impl: NewsRepositoryImpl): NewsRepository

    @Binds
    @Singleton
    abstract fun bindAiInsightCacheRepository(impl: AiInsightCacheRepositoryImpl): AiInsightCacheRepository
}
