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
    factory { CheckGeofenceBreachUseCase() }
    factory { UpdateMemberLocationUseCase(get()) }
    factory { SendBreachNotificationUseCase(get(), get()) }
    factory { GetActiveAlertsUseCase(get()) }
    factory { AcknowledgeAlertUseCase(get()) }
    factory { ToggleTrackingUseCase(get()) }
    factory { TriggerMemberExitUseCase(get()) }
    factory { TriggerMemberReturnUseCase(get()) }
}
