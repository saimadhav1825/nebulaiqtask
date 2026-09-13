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

/**
 * Holds the current user's session profile.
 *
 * On Android, the [userId] is the real Firebase Anonymous Auth UID,
 * injected at startup by [FirebaseAuthManager]. It is stable across
 * app sessions, unlike the old random `usr_XXXX` IDs.
 *
 * [displayName] is persisted by FirebaseAuthManager in SharedPreferences.
 */
class UserSessionManager {

    private val _currentProfile = MutableStateFlow(
        UserProfile(
            userId = "",          // Populated from FirebaseAuthManager at startup
            displayName = "",     // Populated from FirebaseAuthManager/prefs at startup
            role = MemberRole.LEADER,
            avatarColorHex = 0xFF6366F1
        )
    )
    val currentProfile: StateFlow<UserProfile> = _currentProfile.asStateFlow()

    /**
     * Called once at startup (from MainActivity/NebulaApp) after Firebase Auth
     * completes and any saved display name is loaded from SharedPreferences.
     */
    fun initialize(userId: String, displayName: String) {
        val colors = listOf(0xFF6366F1L, 0xFF06B6D4L, 0xFF10B981L, 0xFFF59E0BL, 0xFFEC4899L, 0xFF8B5CF6L)
        // Deterministic color from userId hash so it's consistent across sessions
        val colorIndex = ((userId.hashCode() % colors.size) + colors.size) % colors.size
        _currentProfile.value = UserProfile(
            userId = userId,
            displayName = displayName,
            role = MemberRole.LEADER,
            avatarColorHex = colors[colorIndex]
        )
    }

    /** Update display name and optional role (e.g. when user edits their profile) */
    fun updateProfile(displayName: String, role: MemberRole = MemberRole.MEMBER) {
        val colors = listOf(0xFF6366F1L, 0xFF06B6D4L, 0xFF10B981L, 0xFFF59E0BL, 0xFFEC4899L, 0xFF8B5CF6L)
        val userId = _currentProfile.value.userId
        val colorIndex = ((userId.hashCode() % colors.size) + colors.size) % colors.size
        _currentProfile.value = _currentProfile.value.copy(
            displayName = displayName.ifBlank { _currentProfile.value.displayName },
            role = role,
            avatarColorHex = colors[colorIndex]
        )
    }

    fun getUserId(): String = _currentProfile.value.userId
    fun getDisplayName(): String = _currentProfile.value.displayName
    fun getRole(): MemberRole = _currentProfile.value.role
    fun getAvatarColor(): Long = _currentProfile.value.avatarColorHex
    fun isInitialized(): Boolean = _currentProfile.value.userId.isNotBlank()
}
