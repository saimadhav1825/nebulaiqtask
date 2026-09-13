package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.repository.GeofenceTrackerRepository
import com.app.nebulaiqtask.domain.repository.NotificationRepository

class SendBreachNotificationUseCase(
    private val notificationRepository: NotificationRepository,
    private val geofenceTrackerRepository: GeofenceTrackerRepository
) {
    suspend operator fun invoke(alert: BreachAlert, groupName: String, recipientCount: Int = 9) {
        geofenceTrackerRepository.recordBreachAlert(alert)
        notificationRepository.dispatchBreachNotification(alert, groupName, recipientCount)
    }
}
