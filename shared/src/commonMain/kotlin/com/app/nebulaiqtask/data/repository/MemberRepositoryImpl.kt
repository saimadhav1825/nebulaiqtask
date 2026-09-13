package com.app.nebulaiqtask.data.repository

import com.app.nebulaiqtask.data.datasource.FirebaseGroupDataSource
import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.data.dto.MemberDto
import com.app.nebulaiqtask.data.mapper.LocationMapper
import com.app.nebulaiqtask.data.mapper.MemberMapper
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock as DateTimeClock
import kotlin.random.Random

class MemberRepositoryImpl(
    private val localDataSource: LocalGroupDataSource,
    private val firebaseDataSource: FirebaseGroupDataSource,
    private val memberMapper: MemberMapper,
    private val locationMapper: LocationMapper
) : MemberRepository {

    override fun getMembersFlow(groupId: String): Flow<List<GroupMember>> {
        val sanitized = groupId.trim().uppercase()
        return localDataSource.groups.map { groupsMap ->
            val group = groupsMap[sanitized] ?: groupsMap[groupId]
            group?.members?.map { memberMapper.toDomain(it) } ?: emptyList()
        }
    }

    override suspend fun getMember(memberId: String): GroupMember? {
        val allGroups = localDataSource.groups.value
        for ((_, group) in allGroups) {
            val found = group.members.find { it.id == memberId }
            if (found != null) {
                return memberMapper.toDomain(found)
            }
        }
        return null
    }

    override suspend fun updateMemberLocation(
        groupId: String,
        memberId: String,
        location: LocationCoordinate,
        battery: Int,
        isInside: Boolean,
        distanceToFence: Double
    ) {
        val sanitized = groupId.trim().uppercase()
        val locDto = locationMapper.toDto(location)
        localDataSource.updateSingleMember(
            groupId = sanitized,
            memberId = memberId,
            location = locDto,
            battery = battery,
            isInside = isInside,
            distanceToFence = distanceToFence
        )

        try {
            firebaseDataSource.publishMemberLocation(
                groupCode = sanitized,
                memberId = memberId,
                location = locDto,
                battery = battery,
                isInside = isInside,
                distanceToFence = distanceToFence
            )
        } catch (e: Exception) {
            // Updated in local cache
        }
    }

    override suspend fun addMember(groupId: String, name: String, role: MemberRole): Result<GroupMember> {
        val sanitized = groupId.trim().uppercase()
        val group = localDataSource.getGroup(sanitized)
            ?: return Result.failure(IllegalArgumentException("Group $sanitized not found"))

        val randomDigits = Random.nextInt(1000, 9999)
        val memberId = "usr_$randomDigits"
        val colors = listOf(0xFF6366F1, 0xFF06B6D4, 0xFF10B981, 0xFFF59E0B, 0xFFEC4899, 0xFF8B5CF6)
        val color = colors[Random.nextInt(colors.size)]
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val memberName = name.ifBlank { "Member $randomDigits" }
        val initials = memberName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").uppercase().take(2).ifBlank { "MB" }

        val newMember = MemberDto(
            id = memberId,
            name = memberName,
            role = role.name,
            avatarColorHex = color,
            initials = initials,
            currentLocation = group.geofence.center,
            batteryPercent = 100,
            isInsideGeofence = true,
            distanceToFenceMeters = 0.0,
            isLocalUser = false,
            lastUpdatedMillis = now
        )

        localDataSource.addMember(sanitized, newMember)

        try {
            firebaseDataSource.addMember(sanitized, newMember)
        } catch (e: Exception) {
            // Local member created
        }

        return Result.success(memberMapper.toDomain(newMember))
    }

    override suspend fun removeMember(groupId: String, memberId: String): Result<Unit> {
        val sanitized = groupId.trim().uppercase()
        localDataSource.removeMember(sanitized, memberId)
        try {
            firebaseDataSource.removeMember(sanitized, memberId)
        } catch (e: Exception) {
            // Local removed
        }
        return Result.success(Unit)
    }

    override suspend fun triggerMemberBreach(groupId: String, memberId: String): GroupMember? {
        val sanitized = groupId.trim().uppercase()
        val group = localDataSource.getGroup(sanitized) ?: return null
        val target = group.members.find { it.id == memberId } ?: return null

        // Move member ~450m outside fence north
        val breachedLocation = target.currentLocation.copy(
            latitude = group.geofence.center.latitude + 0.0055,
            longitude = group.geofence.center.longitude,
            timestamp = DateTimeClock.System.now().toEpochMilliseconds()
        )

        val breachedDto = target.copy(
            currentLocation = breachedLocation,
            isInsideGeofence = false,
            distanceToFenceMeters = 150.0,
            lastUpdatedMillis = breachedLocation.timestamp
        )

        localDataSource.updateSingleMember(
            groupId = sanitized,
            memberId = memberId,
            location = breachedLocation,
            battery = target.batteryPercent,
            isInside = false,
            distanceToFence = 150.0
        )

        try {
            firebaseDataSource.publishMemberLocation(
                groupCode = sanitized,
                memberId = memberId,
                location = breachedLocation,
                battery = target.batteryPercent,
                isInside = false,
                distanceToFence = 150.0
            )
        } catch (e: Exception) {
            // Updated local
        }

        return memberMapper.toDomain(breachedDto)
    }

    override suspend fun returnMemberToSafety(groupId: String, memberId: String): GroupMember? {
        val sanitized = groupId.trim().uppercase()
        val group = localDataSource.getGroup(sanitized) ?: return null
        val target = group.members.find { it.id == memberId } ?: return null

        // Return inside safe zone
        val safeLocation = target.currentLocation.copy(
            latitude = group.geofence.center.latitude,
            longitude = group.geofence.center.longitude,
            timestamp = DateTimeClock.System.now().toEpochMilliseconds()
        )

        val safeDto = target.copy(
            currentLocation = safeLocation,
            isInsideGeofence = true,
            distanceToFenceMeters = 0.0,
            lastUpdatedMillis = safeLocation.timestamp
        )

        localDataSource.updateSingleMember(
            groupId = sanitized,
            memberId = memberId,
            location = safeLocation,
            battery = target.batteryPercent,
            isInside = true,
            distanceToFence = 0.0
        )

        try {
            firebaseDataSource.publishMemberLocation(
                groupCode = sanitized,
                memberId = memberId,
                location = safeLocation,
                battery = target.batteryPercent,
                isInside = true,
                distanceToFence = 0.0
            )
        } catch (e: Exception) {
            // Updated local
        }

        return memberMapper.toDomain(safeDto)
    }
}

