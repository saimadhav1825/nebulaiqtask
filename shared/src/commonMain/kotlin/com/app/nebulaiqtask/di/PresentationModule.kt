package com.app.nebulaiqtask.di

import androidx.lifecycle.SavedStateHandle
import com.app.nebulaiqtask.presentation.feature.alerts.viewmodel.AlertsViewModel
import com.app.nebulaiqtask.presentation.feature.creategroup.viewmodel.CreateGroupViewModel
import com.app.nebulaiqtask.presentation.feature.groupdetail.viewmodel.GroupDetailViewModel
import com.app.nebulaiqtask.presentation.feature.home.viewmodel.HomeViewModel
import com.app.nebulaiqtask.presentation.feature.memberdetail.viewmodel.MemberDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { params ->
        HomeViewModel(
            savedStateHandle = params.getOrNull() ?: SavedStateHandle(),
            getActiveGroupIdUseCase = get(),
            saveActiveGroupIdUseCase = get(),
            getTrackingGroupUseCase = get(),
            getGroupMembersUseCase = get(),
            checkGeofenceBreachUseCase = get(),
            joinTrackingGroupUseCase = get(),
            addGroupMemberUseCase = get(),
            removeGroupMemberUseCase = get(),
            sendBreachNotificationUseCase = get(),
            triggerMemberExitUseCase = get(),
            triggerMemberReturnUseCase = get(),
            acknowledgeAlertUseCase = get(),
            toggleTrackingUseCase = get(),
            getActiveAlertsUseCase = get(),
            updateMemberLocationUseCase = get(),
            observeCurrentUserUseCase = get(),
            initializeUserSessionUseCase = get(),
            updateDisplayNameUseCase = get(),
            getCurrentUserUseCase = get(),
            permissionManager = get(),
            locationTracker = get(),
            deviceTelemetry = get(),
            notificationManager = get()
        )

    }

    viewModel { params ->
        CreateGroupViewModel(
            savedStateHandle = params.getOrNull() ?: SavedStateHandle(),
            createTrackingGroupUseCase = get(),
            locationTracker = get()
        )
    }

    viewModel { params ->
        GroupDetailViewModel(
            savedStateHandle = params.getOrNull() ?: SavedStateHandle(),
            getTrackingGroupUseCase = get(),
            getGroupMembersUseCase = get(),
            triggerMemberExitUseCase = get(),
            triggerMemberReturnUseCase = get(),
            checkGeofenceBreachUseCase = get(),
            sendBreachNotificationUseCase = get()
        )
    }

    viewModel { params ->
        MemberDetailViewModel(
            savedStateHandle = params.getOrNull() ?: SavedStateHandle(),
            getGroupMembersUseCase = get(),
            getTrackingGroupUseCase = get(),
            triggerMemberExitUseCase = get(),
            triggerMemberReturnUseCase = get(),
            checkGeofenceBreachUseCase = get(),
            sendBreachNotificationUseCase = get()
        )
    }

    viewModel { params ->
        AlertsViewModel(
            savedStateHandle = params.getOrNull() ?: SavedStateHandle(),
            getActiveAlertsUseCase = get(),
            acknowledgeAlertUseCase = get(),
            getDeliveredNotificationsUseCase = get(),
            clearActiveAlertsUseCase = get()
        )
    }
}
