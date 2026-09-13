package com.app.nebulaiqtask.di

import org.koin.core.module.Module

expect val platformModule: Module

val appModules = listOf(
    platformModule,
    dataModule,
    domainModule,
    presentationModule
)
