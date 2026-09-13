package com.app.nebulaiqtask.presentation.platform

import com.app.nebulaiqtask.domain.model.LocationCoordinate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual class PlatformLocationTracker {
    actual fun startLocationUpdates(): Flow<LocationCoordinate> = emptyFlow()
    actual fun stopLocationUpdates() {}
    actual fun getCurrentLocation(): LocationCoordinate? = null
}
