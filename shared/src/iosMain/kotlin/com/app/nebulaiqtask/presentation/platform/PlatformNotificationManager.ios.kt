package com.app.nebulaiqtask.presentation.platform

actual class PlatformNotificationManager {
    actual fun showHeadsUpBreachNotification(
        title: String,
        message: String,
        breachDistanceMeters: Double,
        memberName: String
    ) {
        println("[iOS] $title: $message (+$breachDistanceMeters m)")
    }

    actual fun playBreachAlertHapticAndAudio() {
        println("[iOS] Playing haptic and audio")
    }
}
