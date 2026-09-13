package com.app.nebulaiqtask.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MemberRole {
    LEADER,
    SAFETY_OFFICER,
    NAVIGATOR,
    MEMBER
}

@Serializable
data class GroupMember(
    val id: String,
    val name: String,
    val role: MemberRole,
    val avatarColorHex: Long,
    val initials: String,
    val currentLocation: LocationCoordinate,
    val isInsideGeofence: Boolean = true,
    val batteryPercent: Int = 100,
    val distanceToFenceMeters: Double = 0.0,
    val lastUpdatedMillis: Long = 0L,
    val isLocalUser: Boolean = false
)
