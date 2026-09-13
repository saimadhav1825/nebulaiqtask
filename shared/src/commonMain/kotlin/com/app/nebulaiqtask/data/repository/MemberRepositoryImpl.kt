package com.app.nebulaiqtask.data.repository

import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.data.mapper.LocationMapper
import com.app.nebulaiqtask.data.mapper.MemberMapper
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemberRepositoryImpl(
    private val dataSource: LocalGroupDataSource,
    private val memberMapper: MemberMapper,
    private val locationMapper: LocationMapper
) : MemberRepository {

    override fun getMembersFlow(groupId: String): Flow<List<GroupMember>> {
        return dataSource.groups.map { groupsMap ->
            val group = groupsMap[groupId]
            group?.members?.map { memberMapper.toDomain(it) } ?: emptyList()
        }
    }

    override suspend fun getMember(memberId: String): GroupMember? {
        val allGroups = dataSource.groups.value
        for ((_, group) in allGroups) {
            val found = group.members.find { it.id == memberId }
            if (found != null) {
                return memberMapper.toDomain(found)
            }
        }
        return null
    }

    override suspend fun updateMemberLocation(
        memberId: String,
        location: LocationCoordinate,
        isInside: Boolean,
        distanceToFence: Double
    ) {
        dataSource.updateSingleMember(
            memberId = memberId,
            location = locationMapper.toDto(location),
            isInside = isInside,
            distanceToFence = distanceToFence
        )
    }

    override suspend fun simulateMovementTick(groupId: String): List<GroupMember> {
        val updatedDtos = dataSource.simulateTick(groupId)
        return updatedDtos.map { memberMapper.toDomain(it) }
    }

    override suspend fun triggerMemberExit(groupId: String, memberId: String): GroupMember? {
        val breachedDto = dataSource.triggerMemberExit(groupId, memberId) ?: return null
        return memberMapper.toDomain(breachedDto)
    }

    override suspend fun triggerMemberReturn(groupId: String, memberId: String): GroupMember? {
        val safeDto = dataSource.triggerMemberReturn(groupId, memberId) ?: return null
        return memberMapper.toDomain(safeDto)
    }
}
