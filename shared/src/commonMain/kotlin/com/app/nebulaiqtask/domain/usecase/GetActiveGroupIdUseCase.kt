package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.repository.UserRepository

class GetActiveGroupIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): String? = repository.getActiveGroupId()
}
