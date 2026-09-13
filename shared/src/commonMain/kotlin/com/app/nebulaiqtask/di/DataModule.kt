package com.app.nebulaiqtask.di

import com.app.nebulaiqtask.data.datasource.FirebaseGroupDataSource
import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.data.mapper.*
import com.app.nebulaiqtask.data.repository.*
import com.app.nebulaiqtask.data.session.UserSessionManager
import com.app.nebulaiqtask.domain.repository.*
import org.koin.dsl.module

val dataModule = module {
    // Mappers
    single { LocationMapper() }
    single { GeofenceMapper(get()) }
    single { MemberMapper(get()) }
    single { GroupMapper(get(), get()) }
    single { AlertMapper(get()) }
    single { NotificationEventMapper() }

    // Session & Data Sources
    single { UserSessionManager() }
    single { FirebaseGroupDataSource() }
    single { LocalGroupDataSource() }

    // Repositories
    single<TrackingGroupRepository> { TrackingGroupRepositoryImpl(get(), get(), get(), get(), get()) }
    single<MemberRepository> { MemberRepositoryImpl(get(), get(), get(), get()) }
    single<GeofenceTrackerRepository> { GeofenceTrackerRepositoryImpl(get(), get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get(), get(), get()) }
}

