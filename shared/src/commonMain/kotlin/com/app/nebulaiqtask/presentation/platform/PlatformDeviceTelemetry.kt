package com.app.nebulaiqtask.presentation.platform

expect class PlatformDeviceTelemetry {
    fun getBatteryPercentage(): Int
    fun isDeviceCharging(): Boolean
}
