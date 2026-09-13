package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.repository.GeofenceTrackerRepository
import com.app.nebulaiqtask.domain.repository.NotificationRepository

class SendBreachNotificationUseCase(
    private val notificationRepository: NotificationRepository,
    private val geofenceTrackerRepository: GeofenceTrackerRepository
) {
    // Tracks member keys currently notified: "$groupId:$memberId"
    private val notifiedMembers = mutableSetOf<String>()

    suspend operator fun invoke(alert: BreachAlert, groupName: String, recipientCount: Int = 9) {
        val key = "${alert.groupId}:${alert.memberId}"
        val isNewNotification = notifiedMembers.add(key)

        if (isNewNotification) {
            geofenceTrackerRepository.recordBreachAlert(alert)
            notificationRepository.dispatchBreachNotification(alert, groupName, recipientCount)
        }
    }

    fun onMemberReturnedToSafety(groupId: String, memberId: String, memberName: String) {
        notifiedMembers.remove("$groupId:$memberId")
        notificationRepository.dismissBreachNotification(memberName)
    }

    fun resetState(groupId: String, memberId: String? = null) {
        if (memberId != null) {
            notifiedMembers.remove("$groupId:$memberId")
        } else {
            notifiedMembers.removeAll { it.startsWith("$groupId:") }
        }
    }
}
