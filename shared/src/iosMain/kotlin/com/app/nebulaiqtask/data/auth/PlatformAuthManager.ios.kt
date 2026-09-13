package com.app.nebulaiqtask.data.auth

import kotlin.random.Random

/**
 * iOS platform implementation for authentication.
 */
actual class PlatformAuthManager {
    private var cachedUid: String? = null

    actual suspend fun getOrCreateUserId(): String {
        if (cachedUid == null) {
            cachedUid = "ios_uid_${Random.nextInt(10000, 99999)}"
        }
        return cachedUid!!
    }

    actual fun getCurrentUserId(): String? = cachedUid

    actual fun isSignedIn(): Boolean = cachedUid != null
}
