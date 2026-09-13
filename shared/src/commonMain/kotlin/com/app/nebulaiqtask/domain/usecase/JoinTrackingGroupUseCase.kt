package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.TrackingGroup
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository

class JoinTrackingGroupUseCase(
    private val repository: TrackingGroupRepository
) {
    suspend operator fun invoke(groupCode: String): Result<TrackingGroup> {
        return repository.joinTrackingGroup(groupCode.trim().uppercase())
    }
}
