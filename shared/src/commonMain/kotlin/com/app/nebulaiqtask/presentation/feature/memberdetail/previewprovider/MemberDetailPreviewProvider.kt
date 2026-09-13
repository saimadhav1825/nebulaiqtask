package com.app.nebulaiqtask.presentation.feature.memberdetail.previewprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.*
import com.app.nebulaiqtask.presentation.feature.memberdetail.state.MemberDetailState

class MemberDetailPreviewProvider : PreviewParameterProvider<MemberDetailState> {
    override val values: Sequence<MemberDetailState> = sequenceOf(
        MemberDetailState(
            isLoading = false,
            member = GroupMember(
                id = "m10",
                name = "Lucas Bennett",
                role = MemberRole.MEMBER,
                avatarColorHex = 0xFFE11D48,
                initials = "LB",
                currentLocation = LocationCoordinate(37.7780, -122.4194),
                isInsideGeofence = false,
                batteryPercent = 72,
                distanceToFenceMeters = 55.0
            ),
            geofence = GeofenceZone(
                id = "fence_1",
                name = "Campus Safety Zone",
                center = LocationCoordinate(37.7749, -122.4194),
                radiusMeters = 300.0
            ),
            groupId = "group_team_alpha"
        )
    )
}
