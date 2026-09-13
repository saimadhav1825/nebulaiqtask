package com.app.nebulaiqtask.presentation.platform

import com.app.nebulaiqtask.domain.model.LocationCoordinate
import kotlinx.coroutines.flow.Flow

expect class PlatformLocationTracker {
    fun startLocationUpdates(): Flow<LocationCoordinate>
    fun stopLocationUpdates()
    fun getCurrentLocation(): LocationCoordinate?
}
