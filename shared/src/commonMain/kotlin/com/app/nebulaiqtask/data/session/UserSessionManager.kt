package com.app.nebulaiqtask.data.session

import com.app.nebulaiqtask.domain.model.MemberRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class UserProfile(
    val userId: String,
    val displayName: String,
    val role: MemberRole = MemberRole.LEADER,
    val avatarColorHex: Long = 0xFF6366F1
)

class UserSessionManager {
    private val randomDigits = Random.nextInt(1000, 9999)
    private val initialProfile = UserProfile(
        userId = "usr_$randomDigits",
        displayName = "Operator $randomDigits",
        role = MemberRole.LEADER,
        avatarColorHex = 0xFF6366F1
    )

    private val _currentProfile = MutableStateFlow(initialProfile)
    val currentProfile: StateFlow<UserProfile> = _currentProfile.asStateFlow()

    fun updateProfile(displayName: String, role: MemberRole = MemberRole.MEMBER) {
        val initials = displayName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").uppercase()
        val colors = listOf(0xFF6366F1, 0xFF06B6D4, 0xFF10B981, 0xFFF59E0B, 0xFFEC4899, 0xFF8B5CF6)
        val color = colors[Random.nextInt(colors.size)]

        _currentProfile.value = _currentProfile.value.copy(
            displayName = displayName.ifBlank { "Member $randomDigits" },
            role = role,
            avatarColorHex = color
        )
    }

    fun getUserId(): String = _currentProfile.value.userId
    fun getDisplayName(): String = _currentProfile.value.displayName
    fun getRole(): MemberRole = _currentProfile.value.role
    fun getAvatarColor(): Long = _currentProfile.value.avatarColorHex
}
