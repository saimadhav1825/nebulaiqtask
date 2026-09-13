package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.TrackingGroup
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository

class ToggleTrackingUseCase(
    private val repository: TrackingGroupRepository
) {
    suspend operator fun invoke(groupId: String, isActive: Boolean): TrackingGroup {
        return repository.toggleTracking(groupId, isActive)
    }
}
