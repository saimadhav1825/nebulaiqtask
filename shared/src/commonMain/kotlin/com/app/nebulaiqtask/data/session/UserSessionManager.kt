package com.app.nebulaiqtask.data.session

import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.model.UserProfile
import com.app.nebulaiqtask.domain.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin session adapter over [UserRepository].
 * The true source of truth is Jetpack DataStore Multiplatform and PlatformAuthManager.
 */
class UserSessionManager(
    private val userRepository: UserRepository
) {
    val currentProfile: StateFlow<UserProfile> get() = userRepository.currentUserProfile

    fun getUserId(): String = userRepository.currentUserProfile.value.userId
    fun getDisplayName(): String = userRepository.currentUserProfile.value.displayName
    fun getRole(): MemberRole = userRepository.currentUserProfile.value.role
    fun getAvatarColor(): Long = userRepository.currentUserProfile.value.avatarColorHex
    fun isInitialized(): Boolean = userRepository.currentUserProfile.value.isInitialized
}
