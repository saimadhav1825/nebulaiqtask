package com.app.nebulaiqtask.presentation.feature.memberdetail.state

import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.GroupMember

data class MemberDetailState(
    val isLoading: Boolean = true,
    val member: GroupMember? = null,
    val geofence: GeofenceZone? = null,
    val groupId: String = ""
)
