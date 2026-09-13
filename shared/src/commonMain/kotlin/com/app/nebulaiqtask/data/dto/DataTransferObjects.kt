package com.app.nebulaiqtask.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 5f,
    val timestamp: Long = 0L
)

@Serializable
data class GeofenceZoneDto(
    val id: String,
    val name: String,
    val center: LocationDto,
    val radiusMeters: Double,
    val description: String = "",
    val alertOnExit: Boolean = true,
    val alertOnEntry: Boolean = false,
    val createdAt: Long = 0L
)

@Serializable
data class MemberDto(
    val id: String,
    val name: String,
    val role: String,
    val avatarColorHex: Long,
    val initials: String,
    val currentLocation: LocationDto,
    val isInsideGeofence: Boolean = true,
    val batteryPercent: Int = 100,
    val distanceToFenceMeters: Double = 0.0,
    val lastUpdatedMillis: Long = 0L,
    val isLocalUser: Boolean = false
)

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val geofence: GeofenceZoneDto,
    val members: List<MemberDto>,
    val activeAlertsCount: Int = 0,
    val isTrackingActive: Boolean = true,
    val createdAt: Long = 0L
)

@Serializable
data class BreachAlertDto(
    val id: String,
    val groupId: String,
    val memberId: String,
    val memberName: String,
    val timestamp: Long,
    val breachLocation: LocationDto,
    val distanceOutsideMeters: Double,
    val severity: String = "CRITICAL",
    val isAcknowledged: Boolean = false,
    val notifiedMembersCount: Int = 9
)

@Serializable
data class NotificationEventDto(
    val id: String,
    val alertId: String,
    val recipientMemberId: String,
    val recipientName: String,
    val message: String,
    val deliveredTimestamp: Long,
    val isDelivered: Boolean = true
)
