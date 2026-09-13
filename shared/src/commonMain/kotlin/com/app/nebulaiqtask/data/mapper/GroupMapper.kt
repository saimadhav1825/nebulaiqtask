package com.app.nebulaiqtask.data.mapper

import com.app.nebulaiqtask.data.dto.GroupDto
import com.app.nebulaiqtask.domain.model.TrackingGroup

class GroupMapper(
    private val geofenceMapper: GeofenceMapper,
    private val memberMapper: MemberMapper
) {
    fun toDomain(dto: GroupDto): TrackingGroup {
        return TrackingGroup(
            id = dto.id,
            name = dto.name,
            geofence = geofenceMapper.toDomain(dto.geofence),
            members = dto.members.map { memberMapper.toDomain(it) },
            activeAlertsCount = dto.activeAlertsCount,
            isTrackingActive = dto.isTrackingActive,
            createdAt = dto.createdAt
        )
    }

    fun toDto(domain: TrackingGroup): GroupDto {
        return GroupDto(
            id = domain.id,
            name = domain.name,
            geofence = geofenceMapper.toDto(domain.geofence),
            members = domain.members.map { memberMapper.toDto(it) },
            activeAlertsCount = domain.activeAlertsCount,
            isTrackingActive = domain.isTrackingActive,
            createdAt = domain.createdAt
        )
    }
}
