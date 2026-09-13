package com.app.nebulaiqtask.domain.util

import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoDistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates the great-circle distance between two points on the Earth using Haversine formula in meters.
     */
    fun calculateDistanceMeters(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val dLat = (endLat - startLat) * (kotlin.math.PI / 180.0)
        val dLon = (endLon - startLon) * (kotlin.math.PI / 180.0)

        val lat1Rad = startLat * (kotlin.math.PI / 180.0)
        val lat2Rad = endLat * (kotlin.math.PI / 180.0)

        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLon / 2.0) * sin(dLon / 2.0)

        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun isCoordinateInsideFence(
        coordinate: LocationCoordinate,
        fence: GeofenceZone
    ): Boolean {
        val distance = calculateDistanceMeters(
            startLat = coordinate.latitude,
            startLon = coordinate.longitude,
            endLat = fence.center.latitude,
            endLon = fence.center.longitude
        )
        return distance <= fence.radiusMeters
    }

    fun distanceOutsideFenceMeters(
        coordinate: LocationCoordinate,
        fence: GeofenceZone
    ): Double {
        val distance = calculateDistanceMeters(
            startLat = coordinate.latitude,
            startLon = coordinate.longitude,
            endLat = fence.center.latitude,
            endLon = fence.center.longitude
        )
        return if (distance > fence.radiusMeters) distance - fence.radiusMeters else 0.0
    }
}
