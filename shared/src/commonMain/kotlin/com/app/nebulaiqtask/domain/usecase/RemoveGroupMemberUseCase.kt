package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.repository.MemberRepository

class RemoveGroupMemberUseCase(
    private val repository: MemberRepository
) {
    suspend operator fun invoke(groupId: String, memberId: String): Result<Unit> {
        return repository.removeMember(groupId, memberId)
    }
}
