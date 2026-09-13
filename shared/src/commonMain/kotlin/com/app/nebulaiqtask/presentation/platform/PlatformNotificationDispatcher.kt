package com.app.nebulaiqtask.presentation.platform

interface PlatformNotificationDispatcher {
    fun showHeadsUpBreachNotification(
        title: String,
        message: String,
        breachDistanceMeters: Double,
        memberName: String
    )
}
