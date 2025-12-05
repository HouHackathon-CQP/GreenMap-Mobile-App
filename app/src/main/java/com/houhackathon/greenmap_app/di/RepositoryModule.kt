package com.houhackathon.greenmap_app.di

import com.houhackathon.greenmap_app.data.repository.HealthRepositoryImpl
import com.houhackathon.greenmap_app.data.repository.AuthRepositoryImpl
import com.houhackathon.greenmap_app.domain.repository.HealthRepository
import com.houhackathon.greenmap_app.domain.repository.AuthRepository
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
}
