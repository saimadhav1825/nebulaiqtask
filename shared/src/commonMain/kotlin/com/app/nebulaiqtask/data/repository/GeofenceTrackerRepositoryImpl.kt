package com.app.nebulaiqtask.data.repository

import com.app.nebulaiqtask.data.datasource.FirebaseGroupDataSource
import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.data.mapper.AlertMapper
import com.app.nebulaiqtask.data.mapper.LocationMapper
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.repository.GeofenceTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeofenceTrackerRepositoryImpl(
    private val dataSource: LocalGroupDataSource,
    private val firebaseDataSource: FirebaseGroupDataSource,
    private val alertMapper: AlertMapper,
    private val locationMapper: LocationMapper
) : GeofenceTrackerRepository {

    override fun getActiveAlertsFlow(groupId: String): Flow<List<BreachAlert>> {
        return dataSource.alerts.map { alertsList ->
            alertsList
                .filter { it.groupId == groupId }
                .map { alertMapper.toDomain(it) }
        }
    }

    override suspend fun recordBreachAlert(alert: BreachAlert) {
        val dto = alertMapper.toDto(alert)
        dataSource.addAlert(dto)
        try {
            firebaseDataSource.publishBreachAlert(alert.groupId, dto)
        } catch (e: Exception) {
            // Alert safely preserved in local state if offline
        }
    }

    override suspend fun acknowledgeAlert(alertId: String) {
        dataSource.acknowledgeAlert(alertId)
    }

    override suspend fun clearAlerts(groupId: String) {
        dataSource.clearAlerts(groupId)
    }

    override fun getUserLiveLocationFlow(): Flow<LocationCoordinate?> {
        return dataSource.userLiveLocation.map { dto ->
            dto?.let { locationMapper.toDomain(it) }
        }
    }

    override suspend fun updateLocalUserLocation(coordinate: LocationCoordinate) {
        dataSource.setUserLiveLocation(locationMapper.toDto(coordinate))
    }
}
