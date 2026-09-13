package com.app.nebulaiqtask.domain.repository

import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun getMembersFlow(groupId: String): Flow<List<GroupMember>>
    suspend fun getMember(memberId: String): GroupMember?
    suspend fun updateMemberLocation(
        memberId: String,
        location: LocationCoordinate,
        isInside: Boolean,
        distanceToFence: Double
    )
    suspend fun simulateMovementTick(groupId: String): List<GroupMember>
    suspend fun triggerMemberExit(groupId: String, memberId: String): GroupMember?
    suspend fun triggerMemberReturn(groupId: String, memberId: String): GroupMember?
}
