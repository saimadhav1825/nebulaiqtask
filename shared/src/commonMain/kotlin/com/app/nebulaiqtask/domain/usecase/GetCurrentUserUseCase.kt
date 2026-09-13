package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.UserProfile
import com.app.nebulaiqtask.domain.repository.UserRepository

class GetCurrentUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): UserProfile = userRepository.getCurrentProfile()
}
