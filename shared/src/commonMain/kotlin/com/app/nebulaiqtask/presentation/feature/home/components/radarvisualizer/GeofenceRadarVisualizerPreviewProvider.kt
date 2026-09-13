package com.app.nebulaiqtask.presentation.feature.home.components.radarvisualizer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.*

data class RadarVisualizerPreviewData(
    val geofence: GeofenceZone,
    val members: List<GroupMember>
)

class GeofenceRadarVisualizerPreviewProvider : PreviewParameterProvider<RadarVisualizerPreviewData> {
    override val values: Sequence<RadarVisualizerPreviewData> = sequenceOf(
        RadarVisualizerPreviewData(
            geofence = GeofenceZone(
                id = "preview_fence",
                name = "Preview Campus Safe Zone",
                center = LocationCoordinate(37.7749, -122.4194),
                radiusMeters = 300.0
            ),
            members = listOf(
                GroupMember(
                    id = "m1",
                    name = "Alex Mercer",
                    role = MemberRole.LEADER,
                    avatarColorHex = 0xFF6366F1,
                    initials = "AM",
                    currentLocation = LocationCoordinate(37.7749, -122.4194),
                    isInsideGeofence = true
                ),
                GroupMember(
                    id = "m10",
                    name = "Lucas Bennett",
                    role = MemberRole.MEMBER,
                    avatarColorHex = 0xFFE11D48,
                    initials = "LB",
                    currentLocation = LocationCoordinate(37.7780, -122.4194),
                    isInsideGeofence = false,
                    distanceToFenceMeters = 45.0
                )
            )
        )
    )
}
