package com.app.nebulaiqtask.presentation.feature.groupdetail.previewprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.*
import com.app.nebulaiqtask.presentation.feature.groupdetail.state.GroupDetailState

class GroupDetailPreviewProvider : PreviewParameterProvider<GroupDetailState> {
    override val values: Sequence<GroupDetailState> = sequenceOf(
        GroupDetailState(
            isLoading = false,
            group = TrackingGroup(
                id = "group_team_alpha",
                name = "Alpha Field Operations (10 Members)",
                geofence = GeofenceZone(
                    id = "fence_1",
                    name = "Mission Bay Campus Safety Zone",
                    center = LocationCoordinate(37.7749, -122.4194),
                    radiusMeters = 300.0
                ),
                members = emptyList(),
                activeAlertsCount = 1,
                isTrackingActive = true
            ),
            members = listOf(
                GroupMember(
                    id = "m1",
                    name = "Alex Mercer",
                    role = MemberRole.LEADER,
                    avatarColorHex = 0xFF6366F1,
                    initials = "AM",
                    currentLocation = LocationCoordinate(37.7749, -122.4194),
                    isInsideGeofence = true,
                    isLocalUser = true
                ),
                GroupMember(
                    id = "m10",
                    name = "Lucas Bennett",
                    role = MemberRole.MEMBER,
                    avatarColorHex = 0xFFE11D48,
                    initials = "LB",
                    currentLocation = LocationCoordinate(37.7780, -122.4194),
                    isInsideGeofence = false,
                    distanceToFenceMeters = 55.0,
                    isLocalUser = false
                )
            )
        )
    )
}
