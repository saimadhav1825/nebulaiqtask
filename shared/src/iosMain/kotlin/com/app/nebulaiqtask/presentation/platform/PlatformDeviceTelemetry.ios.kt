package com.app.nebulaiqtask.presentation.platform

actual class PlatformDeviceTelemetry {
    actual fun getBatteryPercentage(): Int = 95
    actual fun isDeviceCharging(): Boolean = false
}
