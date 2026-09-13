package com.app.nebulaiqtask.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.app.nebulaiqtask.data.auth.PlatformAuthManager
import com.app.nebulaiqtask.data.datastore.USER_PREFERENCES_DATASTORE_FILE
import com.app.nebulaiqtask.data.datastore.createDataStore
import com.app.nebulaiqtask.presentation.platform.*
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single { PlatformAuthManager() }
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = {
                val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null
                )
                requireNotNull(documentDirectory).path + "/$USER_PREFERENCES_DATASTORE_FILE"
            }
        )
    }
    single { PlatformNotificationManager() }
    single { PlatformLocationTracker() }
    single { PlatformPermissionManager() }
    single { PlatformDeviceTelemetry() }
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
