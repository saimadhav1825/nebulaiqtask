package com.app.nebulaiqtask.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.app.nebulaiqtask.data.auth.PlatformAuthManager
import com.app.nebulaiqtask.data.datastore.USER_PREFERENCES_DATASTORE_FILE
import com.app.nebulaiqtask.data.datastore.createDataStore
import com.app.nebulaiqtask.presentation.platform.*
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { PlatformAuthManager() }
    single<DataStore<Preferences>> {
        val context: Context = get()
        createDataStore(
            producePath = {
                context.filesDir.resolve(USER_PREFERENCES_DATASTORE_FILE).absolutePath
            }
        )
    }
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

            override fun dismissBreachNotification(memberName: String) {
                manager.dismissBreachNotification(memberName)
            }
        }
    }
}
