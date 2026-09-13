package com.app.nebulaiqtask.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class UserPreferencesData(
    val userId: String,
    val displayName: String,
    val role: String,
    val avatarColorHex: Long
)

class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_AVATAR_COLOR = longPreferencesKey("avatar_color")
        private const val DEFAULT_COLOR = 0xFF6366F1L
    }

    val userPreferencesFlow: Flow<UserPreferencesData> = dataStore.data
        .catch {
            emit(emptyPreferences())
        }
        .map { prefs ->
            UserPreferencesData(
                userId = prefs[KEY_USER_ID] ?: "",
                displayName = prefs[KEY_DISPLAY_NAME] ?: "",
                role = prefs[KEY_ROLE] ?: "LEADER",
                avatarColorHex = prefs[KEY_AVATAR_COLOR] ?: DEFAULT_COLOR
            )
        }

    suspend fun getUserPreferences(): UserPreferencesData {
        return userPreferencesFlow.first()
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun saveDisplayName(displayName: String) {
        dataStore.edit { prefs ->
            prefs[KEY_DISPLAY_NAME] = displayName.trim()
        }
    }

    suspend fun saveRole(role: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ROLE] = role
        }
    }

    suspend fun saveUserProfile(
        userId: String,
        displayName: String,
        role: String = "LEADER",
        avatarColorHex: Long = DEFAULT_COLOR
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_DISPLAY_NAME] = displayName.trim()
            prefs[KEY_ROLE] = role
            prefs[KEY_AVATAR_COLOR] = avatarColorHex
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
