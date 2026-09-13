package com.app.nebulaiqtask.data.mapper

import com.app.nebulaiqtask.data.dto.LocationDto
import com.app.nebulaiqtask.domain.model.LocationCoordinate

class LocationMapper {
    fun toDomain(dto: LocationDto): LocationCoordinate {
        return LocationCoordinate(
            latitude = dto.latitude,
            longitude = dto.longitude,
            accuracyMeters = dto.accuracyMeters,
            timestamp = dto.timestamp
        )
    }

    fun toDto(domain: LocationCoordinate): LocationDto {
        return LocationDto(
            latitude = domain.latitude,
            longitude = domain.longitude,
            accuracyMeters = domain.accuracyMeters,
            timestamp = domain.timestamp
        )
    }
}
