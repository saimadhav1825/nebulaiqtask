package com.app.nebulaiqtask.data.auth

/**
 * Multiplatform abstraction for authentication.
 * On Android, uses real Firebase Anonymous Auth.
 * On other platforms (iOS), provides platform-appropriate auth identity.
 */
expect class PlatformAuthManager {
    suspend fun getOrCreateUserId(): String
    fun getCurrentUserId(): String?
    fun isSignedIn(): Boolean
}
