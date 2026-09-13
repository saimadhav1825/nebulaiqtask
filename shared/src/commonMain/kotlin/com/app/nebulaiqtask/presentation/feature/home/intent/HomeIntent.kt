package com.app.nebulaiqtask.presentation.feature.home.intent

import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.presentation.feature.home.state.HomeViewMode

sealed interface HomeIntent {
    data object Refresh : HomeIntent
    data object ToggleSimulation : HomeIntent
    data object ToggleTracking : HomeIntent
    data class TriggerBreachForMember(val memberId: String) : HomeIntent
    data class ReturnMemberToSafety(val memberId: String) : HomeIntent
    data class AcknowledgeAlert(val alertId: String) : HomeIntent
    data object OnCreateGroupClicked : HomeIntent
    data class OnGroupCardClicked(val groupId: String) : HomeIntent
    data class OnAlertsClicked(val groupId: String) : HomeIntent
    data class OnMemberClicked(val memberId: String, val groupId: String) : HomeIntent
    data class OnViewModeChanged(val mode: HomeViewMode) : HomeIntent
    data class OnToggleRealDeviceGps(val enabled: Boolean) : HomeIntent
    data object RequestPermissions : HomeIntent
    data object OnPermissionsUpdated : HomeIntent

    // Multi-Device & Firebase Group Management
    data class ShowJoinGroupDialog(val show: Boolean) : HomeIntent
    data class OnJoinGroupCodeChanged(val code: String) : HomeIntent
    data object SubmitJoinGroup : HomeIntent
    data class ShowAddMemberDialog(val show: Boolean) : HomeIntent
    data class OnNewMemberNameChanged(val name: String) : HomeIntent
    data class OnNewMemberRoleChanged(val role: MemberRole) : HomeIntent
    data object SubmitAddMember : HomeIntent
    data class RemoveMember(val memberId: String) : HomeIntent
}

