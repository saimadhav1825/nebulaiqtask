package com.app.nebulaiqtask.presentation.platform

expect class PlatformPermissionManager {
    fun hasLocationPermission(): Boolean
    fun hasNotificationPermission(): Boolean
    fun requestPermissions()
}
