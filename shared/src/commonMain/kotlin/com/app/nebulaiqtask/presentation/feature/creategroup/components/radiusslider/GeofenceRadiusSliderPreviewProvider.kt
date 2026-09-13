package com.app.nebulaiqtask.presentation.feature.creategroup.components.radiusslider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class GeofenceRadiusSliderPreviewProvider : PreviewParameterProvider<Double> {
    override val values: Sequence<Double> = sequenceOf(350.0, 750.0, 1500.0)
}
