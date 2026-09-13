package com.app.nebulaiqtask.presentation.feature.home.components.quickactions

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.model.MemberRole

data class QuickActionsPreviewData(
    val members: List<GroupMember>,
    val isTrackingActive: Boolean,
    val useRealDeviceGps: Boolean = true
)

class HomeQuickActionsPreviewProvider : PreviewParameterProvider<QuickActionsPreviewData> {
    override val values: Sequence<QuickActionsPreviewData> = sequenceOf(
        QuickActionsPreviewData(
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
                    isInsideGeofence = true,
                    isLocalUser = false
                )
            ),
            isTrackingActive = true
        )
    )
}
