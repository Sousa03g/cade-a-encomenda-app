package com.cadeaencomenda.app.di

import android.content.Context
import androidx.room.Room
import com.cadeaencomenda.app.data.local.AppDatabase
import com.cadeaencomenda.app.data.local.dao.PackageDao
import com.cadeaencomenda.app.data.local.dao.TrackingEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePackageDao(database: AppDatabase): PackageDao {
        return database.packageDao()
    }

    @Provides
    @Singleton
    fun provideTrackingEventDao(database: AppDatabase): TrackingEventDao {
        return database.trackingEventDao()
    }
}
