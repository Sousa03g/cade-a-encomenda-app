package com.cadeaencomenda.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadeaencomenda.app.MainActivity
import com.cadeaencomenda.app.R
import com.cadeaencomenda.app.data.preferences.PreferencesManager
import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.repository.TrackingRepository
import com.cadeaencomenda.app.domain.usecase.RefreshTrackingUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class TrackingUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val refreshTrackingUseCase: RefreshTrackingUseCase,
    private val repository: TrackingRepository,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting tracking update worker")

        return try {
            val notificationsEnabled = preferencesManager.notificationsEnabled.first()

            // Map current status before refreshing to detect updates
            val beforePackages = repository.getActivePackages().first()
            val beforeMap = beforePackages.associate { pkg ->
                pkg.trackingCode to (pkg.status to pkg.lastEvent)
            }

            // Refresh all tracking packages
            val refreshResult = refreshTrackingUseCase()
            if (refreshResult.isFailure) {
                Log.w(TAG, "Error refreshing packages: ${refreshResult.exceptionOrNull()?.message}")
            }

            // Check if packages were updated
            if (notificationsEnabled) {
                setupNotificationChannel()

                val afterPackages = repository.getActivePackages().first()
                for (pkg in afterPackages) {
                    val oldInfo = beforeMap[pkg.trackingCode]
                    if (oldInfo != null) {
                        val statusChanged = oldInfo.first != pkg.status
                        val eventChanged = oldInfo.second != pkg.lastEvent
                        if (statusChanged || eventChanged) {
                            showPackageUpdateNotification(pkg)
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in TrackingUpdateWorker: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showPackageUpdateNotification(pkg: TrackingPackage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Skipping notification.")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_TRACKING_CODE, pkg.trackingCode)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            pkg.trackingCode.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = context.getString(pkg.status.displayNameRes)
        val contentText = pkg.lastEvent ?: statusText

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(pkg.displayName)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$statusText: $contentText")
                    .setSummaryText(pkg.carrierName ?: "Encomenda")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            pkg.trackingCode.hashCode(),
            notification
        )
    }

    companion object {
        private const val TAG = "TrackingUpdateWorker"
        const val CHANNEL_ID = "tracking_channel"
        private const val CHANNEL_NAME = "Atualizações de Encomendas"
        private const val CHANNEL_DESCRIPTION = "Notificações de movimentação e entrega de encomendas"
        const val EXTRA_TRACKING_CODE = "trackingCode"
    }
}
