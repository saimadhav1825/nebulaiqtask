package com.app.nebulaiqtask.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AlertSeverity {
    WARNING,
    CRITICAL,
    RESOLVED
}

@Serializable
data class BreachAlert(
    val id: String,
    val groupId: String,
    val memberId: String,
    val memberName: String,
    val timestamp: Long,
    val breachLocation: LocationCoordinate,
    val distanceOutsideMeters: Double,
    val severity: AlertSeverity = AlertSeverity.CRITICAL,
    val isAcknowledged: Boolean = false,
    val notifiedMembersCount: Int = 9
)
