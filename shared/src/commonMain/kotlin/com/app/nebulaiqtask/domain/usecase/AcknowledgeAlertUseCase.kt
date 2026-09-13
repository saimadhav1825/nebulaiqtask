package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.repository.GeofenceTrackerRepository

class AcknowledgeAlertUseCase(
    private val repository: GeofenceTrackerRepository
) {
    suspend operator fun invoke(alertId: String) {
        repository.acknowledgeAlert(alertId)
    }
}
