package com.app.nebulaiqtask.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TrackingGroup(
    val id: String,
    val name: String,
    val geofence: GeofenceZone,
    val members: List<GroupMember>,
    val activeAlertsCount: Int = 0,
    val isTrackingActive: Boolean = true,
    val createdAt: Long = 0L
)
