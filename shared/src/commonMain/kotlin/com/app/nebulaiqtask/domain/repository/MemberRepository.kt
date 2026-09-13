package com.app.nebulaiqtask.domain.repository

import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.model.MemberRole
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun getMembersFlow(groupId: String): Flow<List<GroupMember>>
    suspend fun getMember(memberId: String): GroupMember?
    suspend fun updateMemberLocation(
        groupId: String,
        memberId: String,
        location: LocationCoordinate,
        battery: Int,
        isInside: Boolean,
        distanceToFence: Double
    )
    suspend fun addMember(groupId: String, name: String, role: MemberRole): Result<GroupMember>
    suspend fun removeMember(groupId: String, memberId: String): Result<Unit>
    suspend fun triggerMemberBreach(groupId: String, memberId: String): GroupMember?
    suspend fun returnMemberToSafety(groupId: String, memberId: String): GroupMember?
}
