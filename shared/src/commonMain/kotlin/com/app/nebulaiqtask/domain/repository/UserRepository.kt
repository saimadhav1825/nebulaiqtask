package com.app.nebulaiqtask.domain.repository

import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUserProfile: StateFlow<UserProfile>
    val userProfileFlow: Flow<UserProfile>
    suspend fun getCurrentProfile(): UserProfile
    suspend fun initializeSession(): UserProfile
    suspend fun updateDisplayName(name: String)
    suspend fun updateRole(role: MemberRole)
}
