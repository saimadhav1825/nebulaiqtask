package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.NotificationEvent
import com.app.nebulaiqtask.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetDeliveredNotificationsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<NotificationEvent>> = repository.getDeliveredNotificationsFlow()
}
