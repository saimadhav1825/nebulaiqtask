package com.app.nebulaiqtask.presentation.feature.groupdetail.state

import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.TrackingGroup

data class GroupDetailState(
    val isLoading: Boolean = true,
    val group: TrackingGroup? = null,
    val members: List<GroupMember> = emptyList(),
    val isSimulationActive: Boolean = true,
    val selectedFilter: MemberFilter = MemberFilter.ALL
)

enum class MemberFilter {
    ALL,
    INSIDE,
    OUTSIDE_BREACH
}
