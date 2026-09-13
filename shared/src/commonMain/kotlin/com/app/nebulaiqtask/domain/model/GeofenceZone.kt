package com.app.nebulaiqtask.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GeofenceZone(
    val id: String,
    val name: String,
    val center: LocationCoordinate,
    val radiusMeters: Double,
    val description: String = "",
    val alertOnExit: Boolean = true,
    val alertOnEntry: Boolean = false,
    val createdAt: Long = 0L
)
