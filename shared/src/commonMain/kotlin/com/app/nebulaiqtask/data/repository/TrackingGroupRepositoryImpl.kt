package com.app.nebulaiqtask.data.repository

import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.data.datasource.SimulatedMembersDataSource
import com.app.nebulaiqtask.data.dto.GroupDto
import com.app.nebulaiqtask.data.mapper.GeofenceMapper
import com.app.nebulaiqtask.data.mapper.GroupMapper
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.TrackingGroup
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrackingGroupRepositoryImpl(
    private val dataSource: LocalGroupDataSource,
    private val groupMapper: GroupMapper,
    private val geofenceMapper: GeofenceMapper,
    private val simulatedMembersDataSource: SimulatedMembersDataSource
) : TrackingGroupRepository {

    override fun getTrackingGroupFlow(groupId: String): Flow<TrackingGroup?> {
        return dataSource.groups.map { groupsMap ->
            groupsMap[groupId]?.let { groupMapper.toDomain(it) }
        }
    }

    override suspend fun getTrackingGroup(groupId: String): TrackingGroup? {
        return dataSource.getGroup(groupId)?.let { groupMapper.toDomain(it) }
    }

    override suspend fun createTrackingGroup(name: String, geofence: GeofenceZone): TrackingGroup {
        val now = 1726218000000L + (0..10000).random()
        val id = "group_${now}"
        val geofenceDto = geofenceMapper.toDto(geofence)

        val members = simulatedMembersDataSource.createInitial10Members(
            centerLat = geofence.center.latitude,
            centerLon = geofence.center.longitude,
            radiusMeters = geofence.radiusMeters
        )

        val groupDto = GroupDto(
            id = id,
            name = name,
            geofence = geofenceDto,
            members = members,
            activeAlertsCount = 0,
            isTrackingActive = true,
            createdAt = now
        )

        dataSource.saveGroup(groupDto)
        return groupMapper.toDomain(groupDto)
    }

    override suspend fun updateGeofence(groupId: String, geofence: GeofenceZone): TrackingGroup {
        val existing = dataSource.getGroup(groupId)
            ?: throw IllegalArgumentException("Group with ID $groupId not found")
        val updatedDto = existing.copy(geofence = geofenceMapper.toDto(geofence))
        dataSource.saveGroup(updatedDto)
        return groupMapper.toDomain(updatedDto)
    }

    override suspend fun toggleTracking(groupId: String, isActive: Boolean): TrackingGroup {
        val existing = dataSource.getGroup(groupId)
            ?: throw IllegalArgumentException("Group with ID $groupId not found")
        val updatedDto = existing.copy(isTrackingActive = isActive)
        dataSource.saveGroup(updatedDto)
        return groupMapper.toDomain(updatedDto)
    }

    override fun getAllGroupsFlow(): Flow<List<TrackingGroup>> {
        return dataSource.groups.map { map ->
            map.values.map { groupMapper.toDomain(it) }
        }
    }
}
