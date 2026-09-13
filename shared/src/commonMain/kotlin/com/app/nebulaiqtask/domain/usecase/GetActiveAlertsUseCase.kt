package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.repository.GeofenceTrackerRepository
import kotlinx.coroutines.flow.Flow

class GetActiveAlertsUseCase(
    private val repository: GeofenceTrackerRepository
) {
    operator fun invoke(groupId: String): Flow<List<BreachAlert>> {
        return repository.getActiveAlertsFlow(groupId)
    }
}
