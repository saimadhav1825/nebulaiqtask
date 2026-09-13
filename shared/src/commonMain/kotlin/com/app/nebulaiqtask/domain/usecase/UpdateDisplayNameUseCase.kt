package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.repository.UserRepository

class UpdateDisplayNameUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(displayName: String) {
        userRepository.updateDisplayName(displayName)
    }
}
