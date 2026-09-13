package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.repository.GeofenceTrackerRepository
import com.app.nebulaiqtask.domain.repository.NotificationRepository
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository
import com.app.nebulaiqtask.domain.repository.UserRepository
import kotlinx.datetime.Clock

class SendBreachNotificationUseCase(
    private val notificationRepository: NotificationRepository,
    private val geofenceTrackerRepository: GeofenceTrackerRepository,
    private val userRepository: UserRepository,
    private val trackingGroupRepository: TrackingGroupRepository
) {
    // Tracks member keys currently notified: "$groupId:$memberId"
    private val notifiedMembers = mutableSetOf<String>()
    private val lastAlertTime = mutableMapOf<String, Long>()

    suspend operator fun invoke(
        alert: BreachAlert,
        groupName: String,
        recipientCount: Int = 9,
        isLocalUserOwner: Boolean? = null
    ) {
        val currentUserId = userRepository.currentUserProfile.value.userId

        // 1. The member who stepped outside must NEVER receive a notification on their device
        if (alert.memberId == currentUserId) {
            return
        }

        // 2. ONLY the group OWNER receives breach notifications when joined members step outside
        val isOwner = isLocalUserOwner ?: run {
            val group = trackingGroupRepository.getTrackingGroup(alert.groupId)
            group?.members?.any { (it.id == currentUserId || it.isLocalUser) && it.role == MemberRole.LEADER } ?: false
        }
        if (!isOwner) {
            return
        }

        val key = "${alert.groupId}:${alert.memberId}"
        val now = Clock.System.now().toEpochMilliseconds()
        val lastTime = lastAlertTime[key] ?: 0L

        // 3. Strict single-alert deduplication:
        // Trigger only ONE time per exit event. Do not alert multiple times while outside.
        if (key in notifiedMembers || (now - lastTime < 180_000L)) {
            return
        }

        notifiedMembers.add(key)
        lastAlertTime[key] = now

        geofenceTrackerRepository.recordBreachAlert(alert)
        notificationRepository.dispatchBreachNotification(alert, groupName, recipientCount)
    }

    fun onMemberReturnedToSafety(groupId: String, memberId: String, memberName: String) {
        val key = "$groupId:$memberId"
        notifiedMembers.remove(key)
        lastAlertTime.remove(key)
        notificationRepository.dismissBreachNotification(memberName)
    }

    fun resetState(groupId: String, memberId: String? = null) {
        if (memberId != null) {
            notifiedMembers.remove("$groupId:$memberId")
            lastAlertTime.remove("$groupId:$memberId")
        } else {
            notifiedMembers.removeAll { it.startsWith("$groupId:") }
            lastAlertTime.keys.removeAll { it.startsWith("$groupId:") }
        }
    }
}
