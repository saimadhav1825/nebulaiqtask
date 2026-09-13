package com.app.nebulaiqtask.presentation.feature.alerts.components.notificationlog

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.app.nebulaiqtask.domain.model.NotificationEvent

class DeliveredNotificationCardPreviewProvider : PreviewParameterProvider<NotificationEvent> {
    override val values: Sequence<NotificationEvent> = sequenceOf(
        NotificationEvent(
            id = "notif_1",
            alertId = "alert_1",
            recipientMemberId = "m2",
            recipientName = "Sarah Connor",
            message = "Alert delivered: Lucas Bennett breached fence by 55m",
            deliveredTimestamp = 1726218000000L,
            isDelivered = true
        )
    )
}
