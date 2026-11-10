package com.cadeaencomenda.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cadeaencomenda.app.worker.TrackingUpdateWorker
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class CadeAEncomendaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        initializeAdMob()
        scheduleTrackingUpdates()
    }

    private fun initializeAdMob() {
        MobileAds.initialize(this) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
            com.cadeaencomenda.app.ui.components.InterstitialAdManager.loadAd(this)
        }
    }

    private fun scheduleTrackingUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val trackingWorkRequest = PeriodicWorkRequestBuilder<TrackingUpdateWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(TRACKING_WORK_TAG)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            TRACKING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            trackingWorkRequest
        )
    }

    companion object {
        private const val TAG = "CadeAEncomendaApp"
        private const val TRACKING_WORK_NAME = "tracking_update_work"
        private const val TRACKING_WORK_TAG = "tracking_update"
    }
}
