package com.app.nebulaiqtask.domain.repository

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import kotlinx.coroutines.flow.Flow

interface GeofenceTrackerRepository {
    fun getActiveAlertsFlow(groupId: String): Flow<List<BreachAlert>>
    suspend fun recordBreachAlert(alert: BreachAlert)
    suspend fun acknowledgeAlert(alertId: String)
    suspend fun clearAlerts(groupId: String)
    fun getUserLiveLocationFlow(): Flow<LocationCoordinate?>
    suspend fun updateLocalUserLocation(coordinate: LocationCoordinate)
}
