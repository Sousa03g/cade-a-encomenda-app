package com.cadeaencomenda.app.di

import com.cadeaencomenda.app.data.repository.TrackingRepositoryImpl
import com.cadeaencomenda.app.domain.repository.TrackingRepository
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
    abstract fun bindTrackingRepository(
        impl: TrackingRepositoryImpl
    ): TrackingRepository
}
