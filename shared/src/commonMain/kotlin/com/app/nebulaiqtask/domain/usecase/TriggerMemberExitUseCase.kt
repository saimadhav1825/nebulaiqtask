package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.repository.MemberRepository

class TriggerMemberExitUseCase(
    private val repository: MemberRepository
) {
    suspend operator fun invoke(groupId: String, memberId: String): GroupMember? {
        return repository.triggerMemberExit(groupId, memberId)
    }
}
