package com.app.nebulaiqtask.presentation.feature.alerts.previewprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.AlertSeverity
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.model.NotificationEvent
import com.app.nebulaiqtask.presentation.feature.alerts.state.AlertsState
import com.app.nebulaiqtask.presentation.feature.alerts.state.AlertsTab

class AlertsPreviewProvider : PreviewParameterProvider<AlertsState> {
    override val values: Sequence<AlertsState> = sequenceOf(
        AlertsState(
            alerts = listOf(
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
            ),
            notifications = listOf(
                NotificationEvent(
                    id = "notif_1",
                    alertId = "alert_1",
                    recipientMemberId = "m2",
                    recipientName = "Sarah Connor",
                    message = "Alert delivered: Lucas Bennett breached fence by 55m",
                    deliveredTimestamp = 1726218000000L,
                    isDelivered = true
                )
            ),
            selectedTab = AlertsTab.ACTIVE_BREACHES
        )
    )
}
