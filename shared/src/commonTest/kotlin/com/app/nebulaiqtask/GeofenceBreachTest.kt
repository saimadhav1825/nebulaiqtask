package com.app.nebulaiqtask

import com.app.nebulaiqtask.data.dto.LocationDto
import com.app.nebulaiqtask.data.mapper.*
import com.app.nebulaiqtask.domain.model.*
import com.app.nebulaiqtask.domain.usecase.CheckGeofenceBreachUseCase
import com.app.nebulaiqtask.domain.util.GeoDistanceCalculator
import kotlin.test.*

class GeofenceBreachTest {

    private val center = LocationCoordinate(37.7749, -122.4194)
    private val fence = GeofenceZone(
        id = "test_fence",
        name = "Test Safe Zone",
        center = center,
        radiusMeters = 300.0,
        alertOnExit = true
    )

    @Test
    fun testCoordinateInsideFence() {
        // Point identical to center (0m away)
        val insidePoint = LocationCoordinate(37.7749, -122.4194)
        assertTrue(GeoDistanceCalculator.isCoordinateInsideFence(insidePoint, fence))
        assertEquals(0.0, GeoDistanceCalculator.distanceOutsideFenceMeters(insidePoint, fence))
    }

    @Test
    fun testCoordinateOutsideFence() {
        // Point ~500m north (approx +0.0045 degrees latitude)
        val outsidePoint = LocationCoordinate(37.7795, -122.4194)
        assertFalse(GeoDistanceCalculator.isCoordinateInsideFence(outsidePoint, fence))
        val distanceOutside = GeoDistanceCalculator.distanceOutsideFenceMeters(outsidePoint, fence)
        assertTrue(distanceOutside > 150.0)
    }

    @Test
    fun testCheckGeofenceBreachUseCaseGeneratesAlert() {
        val checkUseCase = CheckGeofenceBreachUseCase()
        val breachedMember = GroupMember(
            id = "m10",
            name = "Lucas Bennett",
            role = MemberRole.MEMBER,
            avatarColorHex = 0xFFE11D48,
            initials = "LB",
            currentLocation = LocationCoordinate(37.7800, -122.4194, 5f, 1726218000000L),
            isInsideGeofence = false
        )

        val result = checkUseCase(
            groupId = "group_test",
            member = breachedMember,
            fence = fence,
            totalGroupMembersCount = 10
        )

        assertFalse(result.isInside)
        assertTrue(result.distanceOutsideMeters > 0.0)
        assertNotNull(result.generatedAlert)
        assertEquals("Lucas Bennett", result.generatedAlert?.memberName)
        assertEquals(9, result.generatedAlert?.notifiedMembersCount)
    }

    @Test
    fun testMappersRoundTrip() {
        val locMapper = LocationMapper()
        val geofenceMapper = GeofenceMapper(locMapper)
        val memberMapper = MemberMapper(locMapper)

        val dto = LocationDto(37.7749, -122.4194, 4f, 1000L)
        val domain = locMapper.toDomain(dto)
        assertEquals(dto.latitude, domain.latitude)
        assertEquals(dto.longitude, domain.longitude)

        val roundTripDto = locMapper.toDto(domain)
        assertEquals(dto, roundTripDto)

        val domainFence = geofenceMapper.toDomain(geofenceMapper.toDto(fence))
        assertEquals(fence.name, domainFence.name)
        assertEquals(fence.radiusMeters, domainFence.radiusMeters)
    }
}
