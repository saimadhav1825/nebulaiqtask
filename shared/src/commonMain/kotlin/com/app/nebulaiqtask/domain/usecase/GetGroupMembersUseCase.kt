package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow

class GetGroupMembersUseCase(
    private val memberRepository: MemberRepository
) {
    operator fun invoke(groupId: String): Flow<List<GroupMember>> {
        return memberRepository.getMembersFlow(groupId)
    }

    suspend fun getMember(memberId: String): GroupMember? {
        return memberRepository.getMember(memberId)
    }
}
