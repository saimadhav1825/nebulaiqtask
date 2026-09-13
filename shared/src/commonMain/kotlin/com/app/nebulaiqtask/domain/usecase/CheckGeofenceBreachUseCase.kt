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
    operator fun invoke(
        groupId: String,
        member: GroupMember,
        fence: GeofenceZone,
        totalGroupMembersCount: Int = 10
    ): GeofenceCheckResult {
        val wasInside = member.isInsideGeofence
        val isNowInside = GeoDistanceCalculator.isCoordinateInsideFence(member.currentLocation, fence)
        val distanceOutside = GeoDistanceCalculator.distanceOutsideFenceMeters(member.currentLocation, fence)

        val transition = when {
            wasInside && !isNowInside -> GeofenceTransition.TRANSITION_EXIT
            !wasInside && isNowInside -> GeofenceTransition.TRANSITION_ENTER
            else -> GeofenceTransition.NONE
        }

        // Only generate new alert when a transition EXIT occurs (or if initial position is already breached)
        val alert = if ((transition == GeofenceTransition.TRANSITION_EXIT || (!isNowInside && member.lastUpdatedMillis == 0L)) && fence.alertOnExit) {
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
}
