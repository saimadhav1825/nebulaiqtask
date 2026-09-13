package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.repository.MemberRepository

class AddGroupMemberUseCase(
    private val repository: MemberRepository
) {
    suspend operator fun invoke(groupId: String, name: String, role: MemberRole): Result<GroupMember> {
        return repository.addMember(groupId, name, role)
    }
}
