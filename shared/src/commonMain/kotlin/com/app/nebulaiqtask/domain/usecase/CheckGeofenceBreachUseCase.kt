package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.AlertSeverity
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.util.GeoDistanceCalculator

data class GeofenceCheckResult(
    val memberId: String,
    val isInside: Boolean,
    val distanceOutsideMeters: Double,
    val generatedAlert: BreachAlert? = null
)

class CheckGeofenceBreachUseCase {
    operator fun invoke(
        groupId: String,
        member: GroupMember,
        fence: GeofenceZone,
        totalGroupMembersCount: Int = 10
    ): GeofenceCheckResult {
        val isInside = GeoDistanceCalculator.isCoordinateInsideFence(member.currentLocation, fence)
        val distanceOutside = GeoDistanceCalculator.distanceOutsideFenceMeters(member.currentLocation, fence)

        val alert = if (!isInside && fence.alertOnExit) {
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
            isInside = isInside,
            distanceOutsideMeters = distanceOutside,
            generatedAlert = alert
        )
    }
}
