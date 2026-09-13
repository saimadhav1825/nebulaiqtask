package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.TrackingGroup
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository
import kotlinx.coroutines.flow.Flow

class GetTrackingGroupUseCase(
    private val repository: TrackingGroupRepository
) {
    operator fun invoke(groupId: String): Flow<TrackingGroup?> {
        return repository.getTrackingGroupFlow(groupId)
    }

    suspend fun getOnce(groupId: String): TrackingGroup? {
        return repository.getTrackingGroup(groupId)
    }
}
