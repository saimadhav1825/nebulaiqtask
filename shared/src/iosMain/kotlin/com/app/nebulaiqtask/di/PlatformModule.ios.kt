package com.app.nebulaiqtask.di

import com.app.nebulaiqtask.presentation.platform.IosNotificationDispatcher
import com.app.nebulaiqtask.presentation.platform.PlatformNotificationDispatcher
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<PlatformNotificationDispatcher> { IosNotificationDispatcher() }
}
