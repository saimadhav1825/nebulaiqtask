package com.app.nebulaiqtask.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Handles real Firebase Anonymous Authentication.
 * Provides a stable UID that persists across app sessions.
 * Display name is stored locally in SharedPreferences.
 */
class FirebaseAuthManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nebula_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DISPLAY_NAME = "display_name"
    }

    /**
     * Signs in anonymously if no user is currently signed in.
     * Returns the stable Firebase UID.
     */
    suspend fun signInAnonymously(): String {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            currentUser.uid
        } else {
            val result = auth.signInAnonymously().await()
            result.user?.uid ?: throw IllegalStateException("Firebase anonymous sign-in failed.")
        }
    }

    /** Returns the current Firebase UID, or null if not signed in yet. */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /** Returns true if the user has already signed in. */
    fun isSignedIn(): Boolean = auth.currentUser != null

    /**
     * Saves the user's display name locally.
     * Called after the user sets their name for the first time.
     */
    fun saveDisplayName(name: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name.trim()).apply()
    }

    /**
     * Returns the saved display name, or null if not set yet.
     */
    fun getDisplayName(): String? = prefs.getString(KEY_DISPLAY_NAME, null)

    /**
     * Returns true if the user has already set a display name.
     */
    fun hasDisplayName(): Boolean = !getDisplayName().isNullOrBlank()
}
