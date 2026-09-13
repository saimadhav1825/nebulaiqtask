package com.app.nebulaiqtask.presentation.feature.home.components.breachbanner

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.AlertSeverity
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.LocationCoordinate

class BreachAlertBannerPreviewProvider : PreviewParameterProvider<BreachAlert?> {
    override val values: Sequence<BreachAlert?> = sequenceOf(
        BreachAlert(
            id = "alert_1",
            groupId = "group_team_alpha",
            memberId = "m10",
            memberName = "Lucas Bennett",
            timestamp = 1726218000000L,
            breachLocation = LocationCoordinate(37.7780, -122.4194),
            distanceOutsideMeters = 55.0,
            severity = AlertSeverity.CRITICAL,
            isAcknowledged = false,
            notifiedMembersCount = 9
        )
    )
}
