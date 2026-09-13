package com.app.nebulaiqtask.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationCoordinate(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 5.0f,
    val timestamp: Long = 0L
)
