package com.app.nebulaiqtask.data.repository

import com.app.nebulaiqtask.data.auth.PlatformAuthManager
import com.app.nebulaiqtask.data.datasource.UserPreferencesDataSource
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.model.UserProfile
import com.app.nebulaiqtask.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserRepositoryImpl(
    private val authManager: PlatformAuthManager,
    private val preferencesDataSource: UserPreferencesDataSource,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : UserRepository {

    private val mutex = Mutex()
    private val colors = listOf(0xFF6366F1L, 0xFF06B6D4L, 0xFF10B981L, 0xFFF59E0BL, 0xFFEC4899L, 0xFF8B5CF6L)

    private val _currentUserProfile = MutableStateFlow(
        UserProfile(userId = "", displayName = "", role = MemberRole.LEADER, avatarColorHex = 0xFF6366F1L)
    )
    override val currentUserProfile: StateFlow<UserProfile> = _currentUserProfile.asStateFlow()

    override val userProfileFlow: Flow<UserProfile> = preferencesDataSource.userPreferencesFlow.map { prefs ->
        val role = runCatching { MemberRole.valueOf(prefs.role) }.getOrDefault(MemberRole.LEADER)
        val color = if (prefs.avatarColorHex != 0L) prefs.avatarColorHex else getColorForId(prefs.userId)
        UserProfile(
            userId = prefs.userId,
            displayName = prefs.displayName,
            role = role,
            avatarColorHex = color
        )
    }.onEach { profile ->
        if (profile.userId.isNotBlank()) {
            _currentUserProfile.value = profile
        }
    }

    init {
        userProfileFlow.launchIn(scope)
    }

    override suspend fun getCurrentProfile(): UserProfile {
        if (_currentUserProfile.value.isInitialized) {
            return _currentUserProfile.value
        }
        val prefs = preferencesDataSource.getUserPreferences()
        if (prefs.userId.isNotBlank()) {
            val role = runCatching { MemberRole.valueOf(prefs.role) }.getOrDefault(MemberRole.LEADER)
            val profile = UserProfile(
                userId = prefs.userId,
                displayName = prefs.displayName,
                role = role,
                avatarColorHex = prefs.avatarColorHex
            )
            _currentUserProfile.value = profile
            return profile
        }
        return initializeSession()
    }

    override suspend fun initializeSession(): UserProfile = mutex.withLock {
        // Retrieve stable Firebase UID from PlatformAuthManager
        val uid = try {
            authManager.getOrCreateUserId()
        } catch (e: Exception) {
            authManager.getCurrentUserId() ?: "offline_${randomDigits()}"
        }

        val existingPrefs = preferencesDataSource.getUserPreferences()
        val displayName = if (existingPrefs.displayName.isNotBlank()) {
            existingPrefs.displayName
        } else {
            "User ${uid.take(4).uppercase()}"
        }

        val role = if (existingPrefs.role.isNotBlank()) {
            runCatching { MemberRole.valueOf(existingPrefs.role) }.getOrDefault(MemberRole.LEADER)
        } else {
            MemberRole.LEADER
        }

        val color = getColorForId(uid)

        preferencesDataSource.saveUserProfile(
            userId = uid,
            displayName = displayName,
            role = role.name,
            avatarColorHex = color
        )

        val profile = UserProfile(
            userId = uid,
            displayName = displayName,
            role = role,
            avatarColorHex = color
        )
        _currentUserProfile.value = profile
        return profile
    }

    override suspend fun updateDisplayName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        preferencesDataSource.saveDisplayName(trimmed)
        _currentUserProfile.value = _currentUserProfile.value.copy(displayName = trimmed)
    }

    override suspend fun updateRole(role: MemberRole) {
        preferencesDataSource.saveRole(role.name)
        _currentUserProfile.value = _currentUserProfile.value.copy(role = role)
    }

    override suspend fun getActiveGroupId(): String? {
        return preferencesDataSource.getActiveGroupId()
    }

    override suspend fun saveActiveGroupId(groupId: String?) {
        preferencesDataSource.saveActiveGroupId(groupId)
    }

    private fun getColorForId(id: String): Long {
        if (id.isBlank()) return 0xFF6366F1L
        val index = ((id.hashCode() % colors.size) + colors.size) % colors.size
        return colors[index]
    }

    private fun randomDigits(): String = kotlin.random.Random.nextInt(1000, 9999).toString()
}
