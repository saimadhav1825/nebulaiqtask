package com.app.nebulaiqtask.presentation.feature.home.state

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.TrackingGroup

data class HomeState(
    val isLoading: Boolean = false,
    val activeGroup: TrackingGroup? = null,
    val members: List<GroupMember> = emptyList(),
    val latestAlert: BreachAlert? = null,
    val isSimulationRunning: Boolean = false,
    val isTrackingActive: Boolean = true,
    val totalBreachesCount: Int = 0,
    val selectedMemberForExitId: String? = null
)
