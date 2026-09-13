package com.app.nebulaiqtask.presentation.platform

expect class PlatformNotificationManager {
    fun showHeadsUpBreachNotification(
        title: String,
        message: String,
        breachDistanceMeters: Double,
        memberName: String
    )
    fun playBreachAlertHapticAndAudio()
}
