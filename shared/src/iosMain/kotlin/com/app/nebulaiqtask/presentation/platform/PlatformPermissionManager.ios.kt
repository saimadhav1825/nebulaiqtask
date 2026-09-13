package com.app.nebulaiqtask.presentation.platform

actual class PlatformPermissionManager {
    actual fun hasLocationPermission(): Boolean = true
    actual fun hasNotificationPermission(): Boolean = true
    actual fun requestPermissions() {}
}
