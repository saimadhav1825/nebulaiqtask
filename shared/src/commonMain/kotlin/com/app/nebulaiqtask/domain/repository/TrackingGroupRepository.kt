package com.app.nebulaiqtask.domain.repository

import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.TrackingGroup
import kotlinx.coroutines.flow.Flow

interface TrackingGroupRepository {
    fun getTrackingGroupFlow(groupId: String): Flow<TrackingGroup?>
    suspend fun getTrackingGroup(groupId: String): TrackingGroup?
    suspend fun createTrackingGroup(name: String, geofence: GeofenceZone): TrackingGroup
    suspend fun joinTrackingGroup(groupCode: String): Result<TrackingGroup>
    suspend fun updateGeofence(groupId: String, geofence: GeofenceZone): TrackingGroup
    suspend fun toggleTracking(groupId: String, isActive: Boolean): TrackingGroup
    fun getAllGroupsFlow(): Flow<List<TrackingGroup>>
}
