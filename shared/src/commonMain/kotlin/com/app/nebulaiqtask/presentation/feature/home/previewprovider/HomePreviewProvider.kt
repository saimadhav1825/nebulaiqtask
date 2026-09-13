package com.app.nebulaiqtask.presentation.feature.home.previewprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.*
import com.app.nebulaiqtask.presentation.feature.home.state.HomeState

class HomePreviewProvider : PreviewParameterProvider<HomeState> {
    override val values: Sequence<HomeState> = sequenceOf(
        HomeState(
            isLoading = false,
            activeGroup = TrackingGroup(
                id = "group_1",
                name = "Alpha Field Operations",
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
                    batteryPercent = 98,
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
                    batteryPercent = 72,
                    distanceToFenceMeters = 55.0
                )
            ),
            latestAlert = BreachAlert(
                id = "alert_1",
                groupId = "group_1",
                memberId = "m10",
                memberName = "Lucas Bennett",
                timestamp = 1726218000000L,
                breachLocation = LocationCoordinate(37.7780, -122.4194),
                distanceOutsideMeters = 55.0,
                severity = AlertSeverity.CRITICAL,
                isAcknowledged = false,
                notifiedMembersCount = 9
            ),
            isSimulationRunning = true,
            isTrackingActive = true,
            totalBreachesCount = 1
        )
    )
}
