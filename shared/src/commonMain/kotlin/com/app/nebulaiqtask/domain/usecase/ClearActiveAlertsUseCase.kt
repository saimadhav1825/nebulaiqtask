package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.repository.GeofenceTrackerRepository

class ClearActiveAlertsUseCase(
    private val repository: GeofenceTrackerRepository
) {
    suspend operator fun invoke(groupId: String) = repository.clearAlerts(groupId)
}
