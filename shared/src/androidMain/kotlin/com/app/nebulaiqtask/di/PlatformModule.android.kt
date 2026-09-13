package com.app.nebulaiqtask.di

import com.app.nebulaiqtask.presentation.platform.*
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { PlatformNotificationManager(get()) }
    single { PlatformLocationTracker(get()) }
    single { PlatformPermissionManager(get()) }
    single { PlatformDeviceTelemetry(get()) }
    single<PlatformNotificationDispatcher> {
        val manager = get<PlatformNotificationManager>()
        object : PlatformNotificationDispatcher {
            override fun showHeadsUpBreachNotification(
                title: String,
                message: String,
                breachDistanceMeters: Double,
                memberName: String
            ) {
                manager.showHeadsUpBreachNotification(title, message, breachDistanceMeters, memberName)
            }
        }
    }
}
