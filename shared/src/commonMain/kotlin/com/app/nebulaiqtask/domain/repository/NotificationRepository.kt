package com.app.nebulaiqtask.domain.repository

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.NotificationEvent
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getDeliveredNotificationsFlow(): Flow<List<NotificationEvent>>
    suspend fun dispatchBreachNotification(alert: BreachAlert, groupName: String, recipientCount: Int)
    fun dismissBreachNotification(memberName: String)
}
