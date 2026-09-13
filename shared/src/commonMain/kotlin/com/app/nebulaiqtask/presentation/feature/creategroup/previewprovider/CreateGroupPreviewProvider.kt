package com.app.nebulaiqtask.presentation.feature.creategroup.previewprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.presentation.feature.creategroup.state.CreateGroupState

class CreateGroupPreviewProvider : PreviewParameterProvider<CreateGroupState> {
    override val values: Sequence<CreateGroupState> = sequenceOf(
        CreateGroupState(
            groupName = "Alpha Field Operations",
            geofenceName = "Mission Bay Campus Safety Zone",
            radiusMeters = 300.0,
            alertOnExit = true
        )
    )
}
