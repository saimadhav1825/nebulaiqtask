package com.app.nebulaiqtask.data.repository

import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.data.dto.NotificationEventDto
import com.app.nebulaiqtask.data.mapper.NotificationEventMapper
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.NotificationEvent
import com.app.nebulaiqtask.domain.repository.NotificationRepository
import com.app.nebulaiqtask.presentation.platform.PlatformNotificationDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(
    private val dataSource: LocalGroupDataSource,
    private val mapper: NotificationEventMapper,
    private val platformDispatcher: PlatformNotificationDispatcher
) : NotificationRepository {

    override fun getDeliveredNotificationsFlow(): Flow<List<NotificationEvent>> {
        return dataSource.notifications.map { list ->
            list.map { mapper.toDomain(it) }
        }
    }

    override suspend fun dispatchBreachNotification(
        alert: BreachAlert,
        groupName: String,
        recipientCount: Int
    ) {
        val distanceInt = alert.distanceOutsideMeters.toInt()
        val title = "⚠️ Geofence Breach: ${alert.memberName}"
        val message = "${alert.memberName} moved $distanceInt m outside the safe zone in $groupName."

        // 1. Post native Android Heads-Up notification
        platformDispatcher.showHeadsUpBreachNotification(
            title = title,
            message = message,
            breachDistanceMeters = alert.distanceOutsideMeters,
            memberName = alert.memberName
        )

        // 2. Generate simulated delivery events for group members
        val group = dataSource.getGroup(alert.groupId)
        val recipients = group?.members?.filter { it.id != alert.memberId } ?: emptyList()

        val events = recipients.map { recipient ->
            NotificationEventDto(
                id = "notif_${recipient.id}_${alert.timestamp}",
                alertId = alert.id,
                recipientMemberId = recipient.id,
                recipientName = recipient.name,
                message = "Alert delivered: ${alert.memberName} breached fence by $distanceInt m",
                deliveredTimestamp = alert.timestamp,
                isDelivered = true
            )
        }

        dataSource.addNotifications(events)
    }
}
