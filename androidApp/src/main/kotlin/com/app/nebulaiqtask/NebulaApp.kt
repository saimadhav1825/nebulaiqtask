package com.app.nebulaiqtask

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.app.nebulaiqtask.di.appModules
import com.app.nebulaiqtask.presentation.platform.AndroidNotificationDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NebulaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
            androidContext(this@NebulaApp)
            modules(appModules)
        }

        // Initialize Android Notification Channels
        createSystemNotificationChannels()
    }

    private fun createSystemNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                AndroidNotificationDispatcher.CHANNEL_ID,
                AndroidNotificationDispatcher.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = AndroidNotificationDispatcher.CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }

            val trackingServiceChannel = NotificationChannel(
                "geofence_tracking_service",
                "Geofence Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps active geofence monitoring running for the tracking group"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannels(listOf(alertChannel, trackingServiceChannel))
        }
    }
}
