package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.repository.MemberRepository

class SimulateMemberMovementUseCase(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(groupId: String): List<GroupMember> {
        return memberRepository.simulateMovementTick(groupId)
    }
}
