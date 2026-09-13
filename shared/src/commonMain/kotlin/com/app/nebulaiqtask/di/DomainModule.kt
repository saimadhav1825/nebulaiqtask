package com.app.nebulaiqtask.di

import com.app.nebulaiqtask.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
    factory { CreateTrackingGroupUseCase(get()) }
    factory { JoinTrackingGroupUseCase(get()) }
    factory { AddGroupMemberUseCase(get()) }
    factory { RemoveGroupMemberUseCase(get()) }
    factory { GetTrackingGroupUseCase(get()) }
    factory { GetGroupMembersUseCase(get()) }
    single { CheckGeofenceBreachUseCase() }
    factory { UpdateMemberLocationUseCase(get()) }
    single { SendBreachNotificationUseCase(get(), get()) }
    factory { GetActiveAlertsUseCase(get()) }
    factory { AcknowledgeAlertUseCase(get()) }
    factory { ToggleTrackingUseCase(get()) }
    factory { TriggerMemberExitUseCase(get()) }
    factory { TriggerMemberReturnUseCase(get()) }

    // User & Session Use Cases
    factory { ObserveCurrentUserUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { InitializeUserSessionUseCase(get()) }
    factory { UpdateDisplayNameUseCase(get()) }
    factory { GetActiveGroupIdUseCase(get()) }
    factory { SaveActiveGroupIdUseCase(get()) }

    // Alert & Notification Use Cases
    factory { GetDeliveredNotificationsUseCase(get()) }
    factory { ClearActiveAlertsUseCase(get()) }
}
