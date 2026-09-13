package com.app.nebulaiqtask.data.mapper

import com.app.nebulaiqtask.data.dto.GeofenceZoneDto
import com.app.nebulaiqtask.domain.model.GeofenceZone

class GeofenceMapper(
    private val locationMapper: LocationMapper
) {
    fun toDomain(dto: GeofenceZoneDto): GeofenceZone {
        return GeofenceZone(
            id = dto.id,
            name = dto.name,
            center = locationMapper.toDomain(dto.center),
            radiusMeters = dto.radiusMeters,
            description = dto.description,
            alertOnExit = dto.alertOnExit,
            alertOnEntry = dto.alertOnEntry,
            createdAt = dto.createdAt
        )
    }

    fun toDto(domain: GeofenceZone): GeofenceZoneDto {
        return GeofenceZoneDto(
            id = domain.id,
            name = domain.name,
            center = locationMapper.toDto(domain.center),
            radiusMeters = domain.radiusMeters,
            description = domain.description,
            alertOnExit = domain.alertOnExit,
            alertOnEntry = domain.alertOnEntry,
            createdAt = domain.createdAt
        )
    }
}
