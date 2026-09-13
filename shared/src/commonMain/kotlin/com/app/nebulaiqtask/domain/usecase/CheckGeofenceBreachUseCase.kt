package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.AlertSeverity
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.util.GeoDistanceCalculator

enum class GeofenceTransition {
    NONE,
    TRANSITION_EXIT,
    TRANSITION_ENTER
}

data class GeofenceCheckResult(
    val memberId: String,
    val isInside: Boolean,
    val distanceOutsideMeters: Double,
    val transition: GeofenceTransition,
    val generatedAlert: BreachAlert? = null
)

class CheckGeofenceBreachUseCase {
    // Tracks member IDs currently in active breached state: "$groupId:$memberId"
    private val activeBreachedMembers = mutableSetOf<String>()

    operator fun invoke(
        groupId: String,
        member: GroupMember,
        fence: GeofenceZone,
        totalGroupMembersCount: Int = 10,
        isInitialJoinOrSync: Boolean = false
    ): GeofenceCheckResult {
        val key = "$groupId:${member.id}"
        val isNowInside = GeoDistanceCalculator.isCoordinateInsideFence(member.currentLocation, fence)
        val distanceOutside = GeoDistanceCalculator.distanceOutsideFenceMeters(member.currentLocation, fence)

        val isAlreadyAlerted = synchronized(activeBreachedMembers) { key in activeBreachedMembers }

        val transition = when {
            // 1. Member is safely inside the geofence
            isNowInside -> {
                if (isAlreadyAlerted || !member.isInsideGeofence) {
                    // Transition: was outside, now returned safely inside
                    synchronized(activeBreachedMembers) { activeBreachedMembers.remove(key) }
                    GeofenceTransition.TRANSITION_ENTER
                } else {
                    GeofenceTransition.NONE
                }
            }
            // 2. Member is outside the geofence
            else -> {
                if (isInitialJoinOrSync) {
                    // Initial join or initial sync while already outside: mark as breached but DO NOT fire notification
                    synchronized(activeBreachedMembers) { activeBreachedMembers.add(key) }
                    GeofenceTransition.NONE
                } else if (!isAlreadyAlerted && member.isInsideGeofence) {
                    // Genuine exit transition: was previously inside, now stepped outside
                    synchronized(activeBreachedMembers) { activeBreachedMembers.add(key) }
                    GeofenceTransition.TRANSITION_EXIT
                } else {
                    // Already outside or already alerted: keep in set, no duplicate transition or alert
                    synchronized(activeBreachedMembers) { activeBreachedMembers.add(key) }
                    GeofenceTransition.NONE
                }
            }
        }

        // Only generate new alert on genuine TRANSITION_EXIT when alertOnExit is enabled
        val alert = if (transition == GeofenceTransition.TRANSITION_EXIT && fence.alertOnExit) {
            BreachAlert(
                id = "alert_${member.id}_${member.currentLocation.timestamp}",
                groupId = groupId,
                memberId = member.id,
                memberName = member.name,
                timestamp = member.currentLocation.timestamp,
                breachLocation = member.currentLocation,
                distanceOutsideMeters = distanceOutside,
                severity = if (distanceOutside > 100.0) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
                isAcknowledged = false,
                notifiedMembersCount = (totalGroupMembersCount - 1).coerceAtLeast(1)
            )
        } else null

        return GeofenceCheckResult(
            memberId = member.id,
            isInside = isNowInside,
            distanceOutsideMeters = distanceOutside,
            transition = transition,
            generatedAlert = alert
        )
    }

    fun markAsInitiallyOutside(groupId: String, memberId: String) {
        val key = "$groupId:$memberId"
        synchronized(activeBreachedMembers) {
            activeBreachedMembers.add(key)
        }
    }

    fun resetBreachState(groupId: String, memberId: String? = null) {
        synchronized(activeBreachedMembers) {
            if (memberId != null) {
                activeBreachedMembers.remove("$groupId:$memberId")
            } else {
                activeBreachedMembers.removeAll { it.startsWith("$groupId:") }
            }
        }
    }
}
