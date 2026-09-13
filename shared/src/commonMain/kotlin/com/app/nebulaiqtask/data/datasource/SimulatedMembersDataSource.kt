package com.app.nebulaiqtask.data.datasource

import com.app.nebulaiqtask.data.dto.LocationDto
import com.app.nebulaiqtask.data.dto.MemberDto
import kotlin.math.cos
import kotlin.math.sin

class SimulatedMembersDataSource {

    fun createInitial10Members(centerLat: Double, centerLon: Double, radiusMeters: Double): List<MemberDto> {
        val now = 1726218000000L // Baseline timestamp

        val templates = listOf(
            MemberTemplate("m1", "Alex Mercer", "LEADER", 0xFF6366F1, "AM", 0.0, 0.0, 98, isLocal = true),
            MemberTemplate("m2", "Sarah Connor", "SAFETY_OFFICER", 0xFF10B981, "SC", 0.35, 0.20, 85),
            MemberTemplate("m3", "Michael Chen", "NAVIGATOR", 0xFF06B6D4, "MC", -0.40, 0.30, 92),
            MemberTemplate("m4", "Emma Watson", "MEMBER", 0xFFEC4899, "EW", 0.20, -0.45, 76),
            MemberTemplate("m5", "David Miller", "MEMBER", 0xFFF59E0B, "DM", -0.30, -0.35, 68),
            MemberTemplate("m6", "Olivia Taylor", "MEMBER", 0xFF8B5CF6, "OT", 0.50, -0.15, 89),
            MemberTemplate("m7", "James Wilson", "MEMBER", 0xFF14B8A6, "JW", -0.25, 0.55, 94),
            MemberTemplate("m8", "Sophia Rodriguez", "MEMBER", 0xFFF43F5E, "SR", 0.15, 0.60, 81),
            MemberTemplate("m9", "Daniel Brooks", "MEMBER", 0xFF3B82F6, "DB", -0.55, -0.10, 63),
            MemberTemplate("m10", "Lucas Bennett", "MEMBER", 0xFFE11D48, "LB", 0.65, 0.40, 72)
        )

        // 1 degree latitude ~ 111,000 meters
        val metersToLat = 1.0 / 111000.0
        val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))

        return templates.map { t ->
            val offsetLatMeters = t.fractionX * (radiusMeters * 0.65)
            val offsetLonMeters = t.fractionY * (radiusMeters * 0.65)

            val memberLat = centerLat + (offsetLatMeters * metersToLat)
            val memberLon = centerLon + (offsetLonMeters * metersToLon)

            MemberDto(
                id = t.id,
                name = t.name,
                role = t.role,
                avatarColorHex = t.colorHex,
                initials = t.initials,
                currentLocation = LocationDto(
                    latitude = memberLat,
                    longitude = memberLon,
                    accuracyMeters = 4.5f,
                    timestamp = now
                ),
                isInsideGeofence = true,
                batteryPercent = t.battery,
                distanceToFenceMeters = 0.0,
                lastUpdatedMillis = now,
                isLocalUser = t.isLocal
            )
        }
    }

    fun stepSimulation(
        members: List<MemberDto>,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        tickCount: Long
    ): List<MemberDto> {
        val metersToLat = 1.0 / 111000.0
        val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))

        return members.mapIndexed { index, member ->
            if (member.isLocalUser) return@mapIndexed member

            // Subtle wander trajectory for active members
            val angle = (tickCount * 0.15 + index * 0.75)
            val wanderLatMeters = sin(angle) * 3.5
            val wanderLonMeters = cos(angle) * 3.5

            val newLat = member.currentLocation.latitude + (wanderLatMeters * metersToLat)
            val newLon = member.currentLocation.longitude + (wanderLonMeters * metersToLon)

            member.copy(
                currentLocation = member.currentLocation.copy(
                    latitude = newLat,
                    longitude = newLon,
                    timestamp = member.currentLocation.timestamp + 3000L
                ),
                lastUpdatedMillis = member.lastUpdatedMillis + 3000L
            )
        }
    }

    fun forceBreachMember(
        member: MemberDto,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double
    ): MemberDto {
        val metersToLat = 1.0 / 111000.0
        val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))

        // Move member 55 meters outside the geofence perimeter
        val breachDistanceMeters = radiusMeters + 55.0
        val angle = (member.id.hashCode() % 360) * (kotlin.math.PI / 180.0)

        val newLat = centerLat + (sin(angle) * breachDistanceMeters * metersToLat)
        val newLon = centerLon + (cos(angle) * breachDistanceMeters * metersToLon)

        return member.copy(
            currentLocation = member.currentLocation.copy(
                latitude = newLat,
                longitude = newLon,
                timestamp = member.currentLocation.timestamp + 1000L
            ),
            isInsideGeofence = false,
            distanceToFenceMeters = 55.0,
            lastUpdatedMillis = member.lastUpdatedMillis + 1000L
        )
    }

    fun returnMemberToSafety(
        member: MemberDto,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double
    ): MemberDto {
        val metersToLat = 1.0 / 111000.0
        val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))

        // Move member comfortably inside (40% of radius from center)
        val safeDistanceMeters = radiusMeters * 0.40
        val angle = (member.id.hashCode() % 360) * (kotlin.math.PI / 180.0)

        val newLat = centerLat + (sin(angle) * safeDistanceMeters * metersToLat)
        val newLon = centerLon + (cos(angle) * safeDistanceMeters * metersToLon)

        return member.copy(
            currentLocation = member.currentLocation.copy(
                latitude = newLat,
                longitude = newLon,
                timestamp = member.currentLocation.timestamp + 1000L
            ),
            isInsideGeofence = true,
            distanceToFenceMeters = 0.0,
            lastUpdatedMillis = member.lastUpdatedMillis + 1000L
        )
    }

    private data class MemberTemplate(
        val id: String,
        val name: String,
        val role: String,
        val colorHex: Long,
        val initials: String,
        val fractionX: Double,
        val fractionY: Double,
        val battery: Int,
        val isLocal: Boolean = false
    )
}
