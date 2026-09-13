package com.app.nebulaiqtask.domain.model

data class UserProfile(
    val userId: String,
    val displayName: String,
    val role: MemberRole = MemberRole.LEADER,
    val avatarColorHex: Long = 0xFF6366F1L
) {
    val isInitialized: Boolean get() = userId.isNotBlank()
    val initials: String get() = displayName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .joinToString("")
        .uppercase()
        .take(2)
        .ifBlank { if (userId.isNotBlank()) "OP" else "" }
}
