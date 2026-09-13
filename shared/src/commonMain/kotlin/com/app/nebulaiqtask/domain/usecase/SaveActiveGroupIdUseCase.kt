package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.repository.UserRepository

class SaveActiveGroupIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(groupId: String?) = repository.saveActiveGroupId(groupId)
}
