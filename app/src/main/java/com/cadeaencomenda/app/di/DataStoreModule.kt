package com.cadeaencomenda.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.cadeaencomenda.app.data.preferences.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val PREFERENCES_DATA_STORE_NAME = "cade_a_encomenda_preferences"

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(PREFERENCES_DATA_STORE_NAME) }
        )
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        dataStore: DataStore<Preferences>
    ): PreferencesManager {
        return PreferencesManager(dataStore)
    }
}
