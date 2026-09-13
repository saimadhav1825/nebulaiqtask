package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.UserProfile
import com.app.nebulaiqtask.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<UserProfile> = userRepository.userProfileFlow
}
