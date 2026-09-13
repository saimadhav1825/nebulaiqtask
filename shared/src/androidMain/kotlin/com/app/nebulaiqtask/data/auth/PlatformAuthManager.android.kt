package com.app.nebulaiqtask.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Real Firebase Anonymous Authentication implementation for Android.
 * Provides a stable Firebase UID that persists across app sessions.
 */
actual class PlatformAuthManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    actual suspend fun getOrCreateUserId(): String {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            currentUser.uid
        } else {
            val result = auth.signInAnonymously().await()
            result.user?.uid ?: throw IllegalStateException("Firebase anonymous sign-in failed: user was null")
        }
    }

    actual fun getCurrentUserId(): String? = auth.currentUser?.uid

    actual fun isSignedIn(): Boolean = auth.currentUser != null
}
