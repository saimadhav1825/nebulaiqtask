package com.app.nebulaiqtask.domain.usecase

import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.TrackingGroup
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository

class CreateTrackingGroupUseCase(
    private val repository: TrackingGroupRepository
) {
    suspend operator fun invoke(name: String, geofence: GeofenceZone): TrackingGroup {
        return repository.createTrackingGroup(name, geofence)
    }
}
