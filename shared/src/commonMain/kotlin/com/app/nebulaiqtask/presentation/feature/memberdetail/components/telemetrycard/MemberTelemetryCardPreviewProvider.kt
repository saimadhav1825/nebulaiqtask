package com.app.nebulaiqtask.presentation.feature.memberdetail.components.telemetrycard

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.model.MemberRole

class MemberTelemetryCardPreviewProvider : PreviewParameterProvider<GroupMember> {
    override val values: Sequence<GroupMember> = sequenceOf(
        GroupMember(
            id = "m10",
            name = "Lucas Bennett",
            role = MemberRole.MEMBER,
            avatarColorHex = 0xFFE11D48,
            initials = "LB",
            currentLocation = LocationCoordinate(37.7780, -122.4194),
            isInsideGeofence = false,
            batteryPercent = 72,
            distanceToFenceMeters = 55.0
        )
    )
}
