package com.app.nebulaiqtask.presentation.feature.home.state

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.model.TrackingGroup

enum class HomeViewMode {
    MAP,
    RADAR
}

data class HomeState(
    val isLoading: Boolean = false,
    val activeGroup: TrackingGroup? = null,
    val members: List<GroupMember> = emptyList(),
    val latestAlert: BreachAlert? = null,
    val isSimulationRunning: Boolean = false,
    val isTrackingActive: Boolean = true,
    val totalBreachesCount: Int = 0,
    val selectedMemberForExitId: String? = null,
    val viewMode: HomeViewMode = HomeViewMode.MAP,
    val hasLocationPermission: Boolean = true,
    val hasNotificationPermission: Boolean = true,
    val useRealDeviceGps: Boolean = true,
    val deviceBatteryPercent: Int = 95,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val isJoinGroupDialogVisible: Boolean = false,
    val joinGroupCodeInput: String = "",
    val isAddMemberDialogVisible: Boolean = false,
    val newMemberNameInput: String = "",
    val newMemberRole: MemberRole = MemberRole.MEMBER,
    val isSubmittingAction: Boolean = false
)

