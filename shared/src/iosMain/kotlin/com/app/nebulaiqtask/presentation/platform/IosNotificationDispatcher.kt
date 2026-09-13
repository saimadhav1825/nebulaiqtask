package com.app.nebulaiqtask.presentation.platform

class IosNotificationDispatcher : PlatformNotificationDispatcher {
    override fun showHeadsUpBreachNotification(
        title: String,
        message: String,
        breachDistanceMeters: Double,
        memberName: String
    ) {
        // iOS push / local notification stub for KMP
        println("[iOS Notification] $title: $message (+$breachDistanceMeters m)")
    }
}
