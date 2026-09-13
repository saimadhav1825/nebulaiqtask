package com.app.nebulaiqtask.data.mapper

import com.app.nebulaiqtask.data.dto.MemberDto
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.MemberRole

class MemberMapper(
    private val locationMapper: LocationMapper
) {
    fun toDomain(dto: MemberDto): GroupMember {
        return GroupMember(
            id = dto.id,
            name = dto.name,
            role = try {
                MemberRole.valueOf(dto.role)
            } catch (e: Exception) {
                MemberRole.MEMBER
            },
            avatarColorHex = dto.avatarColorHex,
            initials = dto.initials,
            currentLocation = locationMapper.toDomain(dto.currentLocation),
            isInsideGeofence = dto.isInsideGeofence,
            batteryPercent = dto.batteryPercent,
            distanceToFenceMeters = dto.distanceToFenceMeters,
            lastUpdatedMillis = dto.lastUpdatedMillis,
            isLocalUser = dto.isLocalUser
        )
    }

    fun toDto(domain: GroupMember): MemberDto {
        return MemberDto(
            id = domain.id,
            name = domain.name,
            role = domain.role.name,
            avatarColorHex = domain.avatarColorHex,
            initials = domain.initials,
            currentLocation = locationMapper.toDto(domain.currentLocation),
            isInsideGeofence = domain.isInsideGeofence,
            batteryPercent = domain.batteryPercent,
            distanceToFenceMeters = domain.distanceToFenceMeters,
            lastUpdatedMillis = domain.lastUpdatedMillis,
            isLocalUser = domain.isLocalUser
        )
    }
}
