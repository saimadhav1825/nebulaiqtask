package com.app.nebulaiqtask.data.repository

import com.app.nebulaiqtask.data.datasource.FirebaseGroupDataSource
import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.data.dto.GroupDto
import com.app.nebulaiqtask.data.dto.MemberDto
import com.app.nebulaiqtask.data.dto.MemberRoleDto
import com.app.nebulaiqtask.data.mapper.GeofenceMapper
import com.app.nebulaiqtask.data.mapper.GroupMapper
import com.app.nebulaiqtask.data.session.UserSessionManager
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.TrackingGroup
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class TrackingGroupRepositoryImpl(
    private val localDataSource: LocalGroupDataSource,
    private val firebaseDataSource: FirebaseGroupDataSource,
    private val userSessionManager: UserSessionManager,
    private val groupMapper: GroupMapper,
    private val geofenceMapper: GeofenceMapper
) : TrackingGroupRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun getTrackingGroupFlow(groupId: String): Flow<TrackingGroup?> {
        val sanitizedId = groupId.trim().uppercase()

        // Launch background Firebase observer for this group
        scope.launch {
            try {
                firebaseDataSource.observeGroup(sanitizedId).collect { remoteGroup ->
                    if (remoteGroup != null) {
                        val currentUserId = userSessionManager.getUserId()
                        val updatedMembers = remoteGroup.members.map { m ->
                            m.copy(isLocalUser = m.id == currentUserId)
                        }
                        localDataSource.saveGroup(remoteGroup.copy(members = updatedMembers))
                    }
                }
            } catch (e: Exception) {
                // Keep local cache on network error
            }
        }

        return localDataSource.groups.map { groupsMap ->
            val group = groupsMap[sanitizedId] ?: groupsMap[groupId]
            group?.let { groupMapper.toDomain(it) }
        }
    }

    override suspend fun getTrackingGroup(groupId: String): TrackingGroup? {
        val sanitized = groupId.trim().uppercase()
        // Try local first
        val local = localDataSource.getGroup(sanitized) ?: localDataSource.getGroup(groupId)
        if (local != null) return groupMapper.toDomain(local)

        // Try Firebase
        return try {
            val remote = firebaseDataSource.getGroup(sanitized).getOrNull()
            if (remote != null) {
                val currentUserId = userSessionManager.getUserId()
                val updatedMembers = remote.members.map { m ->
                    m.copy(isLocalUser = m.id == currentUserId)
                }
                val finalGroup = remote.copy(members = updatedMembers)
                localDataSource.saveGroup(finalGroup)
                groupMapper.toDomain(finalGroup)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createTrackingGroup(name: String, geofence: GeofenceZone): TrackingGroup {
        val now = 1726218000000L + (0..100000).random()
        val randomDigits = Random.nextInt(1000, 9999)
        val inviteCode = "NEB-$randomDigits"
        val geofenceDto = geofenceMapper.toDto(geofence)

        val profile = userSessionManager.currentProfile.value
        val leaderMember = MemberDto(
            id = profile.userId,
            name = profile.displayName,
            role = MemberRoleDto.LEADER,
            avatarColorHex = profile.avatarColorHex,
            currentLocation = geofenceDto.center,
            batteryPercent = 100,
            isInsideGeofence = true,
            distanceToFenceMeters = 0.0,
            isLocalUser = true,
            lastUpdatedMillis = now
        )

        val groupDto = GroupDto(
            id = inviteCode,
            name = name.ifBlank { "Tracking Team $inviteCode" },
            geofence = geofenceDto,
            members = listOf(leaderMember),
            activeAlertsCount = 0,
            isTrackingActive = true,
            createdAt = now
        )

        // Save to local cache immediately
        localDataSource.saveGroup(groupDto)

        // Sync to Firebase
        try {
            firebaseDataSource.saveGroup(groupDto)
        } catch (e: Exception) {
            // Local group is still created and will sync when network is active
        }

        return groupMapper.toDomain(groupDto)
    }

    override suspend fun joinTrackingGroup(groupCode: String): Result<TrackingGroup> {
        val sanitizedCode = groupCode.trim().uppercase()
        val now = 1726218000000L + (0..100000).random()
        val profile = userSessionManager.currentProfile.value

        val newMember = MemberDto(
            id = profile.userId,
            name = profile.displayName,
            role = MemberRoleDto.MEMBER,
            avatarColorHex = profile.avatarColorHex,
            currentLocation = localDataSource.userLiveLocation.value ?: com.app.nebulaiqtask.data.dto.LocationDto(37.7749, -122.4194, 3.5f, now),
            batteryPercent = 100,
            isInsideGeofence = true,
            distanceToFenceMeters = 0.0,
            isLocalUser = true,
            lastUpdatedMillis = now
        )

        return try {
            val remoteResult = firebaseDataSource.joinGroup(sanitizedCode, newMember)
            if (remoteResult.isSuccess) {
                val groupDto = remoteResult.getOrThrow()
                val currentUserId = profile.userId
                val updatedMembers = groupDto.members.map { m ->
                    m.copy(isLocalUser = m.id == currentUserId)
                }
                val finalGroup = groupDto.copy(members = updatedMembers)
                localDataSource.saveGroup(finalGroup)
                Result.success(groupMapper.toDomain(finalGroup))
            } else {
                Result.failure(remoteResult.exceptionOrNull() ?: IllegalStateException("Could not join group"))
            }
        } catch (e: Exception) {
            // If offline or group in local cache
            val existing = localDataSource.getGroup(sanitizedCode)
            if (existing != null) {
                val updatedMembers = (existing.members.filterNot { it.id == newMember.id } + newMember)
                val updated = existing.copy(members = updatedMembers)
                localDataSource.saveGroup(updated)
                Result.success(groupMapper.toDomain(updated))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateGeofence(groupId: String, geofence: GeofenceZone): TrackingGroup {
        val sanitized = groupId.trim().uppercase()
        val existing = localDataSource.getGroup(sanitized)
            ?: throw IllegalArgumentException("Group with ID $sanitized not found")
        val updatedDto = existing.copy(geofence = geofenceMapper.toDto(geofence))
        localDataSource.saveGroup(updatedDto)
        try {
            firebaseDataSource.saveGroup(updatedDto)
        } catch (e: Exception) {
            // Local updated
        }
        return groupMapper.toDomain(updatedDto)
    }

    override suspend fun toggleTracking(groupId: String, isActive: Boolean): TrackingGroup {
        val sanitized = groupId.trim().uppercase()
        val existing = localDataSource.getGroup(sanitized)
            ?: throw IllegalArgumentException("Group with ID $sanitized not found")
        val updatedDto = existing.copy(isTrackingActive = isActive)
        localDataSource.saveGroup(updatedDto)
        try {
            firebaseDataSource.saveGroup(updatedDto)
        } catch (e: Exception) {
            // Local updated
        }
        return groupMapper.toDomain(updatedDto)
    }

    override fun getAllGroupsFlow(): Flow<List<TrackingGroup>> {
        return localDataSource.groups.map { map ->
            map.values.map { groupMapper.toDomain(it) }
        }
    }
}

