package com.app.nebulaiqtask.presentation.feature.groupdetail.components.geofencecard

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.LocationCoordinate

class GeofenceZoneCardPreviewProvider : PreviewParameterProvider<GeofenceZone> {
    override val values: Sequence<GeofenceZone> = sequenceOf(
        GeofenceZone(
            id = "preview_fence",
            name = "Mission Bay Campus Safety Zone",
            center = LocationCoordinate(37.7749, -122.4194),
            radiusMeters = 300.0,
            description = "Central campus perimeter & geofenced safety boundary"
        )
    )
}
