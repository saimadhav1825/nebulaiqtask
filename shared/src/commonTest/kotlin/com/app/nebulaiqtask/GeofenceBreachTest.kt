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
            isInsideGeofence = true // was previously inside
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
    fun testInitialJoinWhileOutsideDoesNotGenerateAlert() {
        val checkUseCase = CheckGeofenceBreachUseCase()
        val outsideJoiningMember = GroupMember(
            id = "m11",
            name = "Sarah Connor",
            role = MemberRole.MEMBER,
            avatarColorHex = 0xFF3B82F6,
            initials = "SC",
            currentLocation = LocationCoordinate(37.7850, -122.4194, 5f, 1726218000000L),
            isInsideGeofence = false
        )

        // Case 1: Initial join or sync flag is passed
        val resultWithFlag = checkUseCase(
            groupId = "group_test",
            member = outsideJoiningMember,
            fence = fence,
            totalGroupMembersCount = 10,
            isInitialJoinOrSync = true
        )
        assertFalse(resultWithFlag.isInside)
        assertNull(resultWithFlag.generatedAlert, "Initial join outside must NOT generate breach alert")

        // Case 2: Member already outside without flag
        val freshUseCase = CheckGeofenceBreachUseCase()
        val resultWithoutFlag = freshUseCase(
            groupId = "group_test",
            member = outsideJoiningMember,
            fence = fence,
            totalGroupMembersCount = 10
        )
        assertNull(resultWithoutFlag.generatedAlert, "User already outside must NOT generate breach alert")
    }

    @Test
    fun testRepeatedCheckWhileOutsideDoesNotRepeatAlert() {
        val checkUseCase = CheckGeofenceBreachUseCase()
        val member = GroupMember(
            id = "m12",
            name = "John Doe",
            role = MemberRole.MEMBER,
            avatarColorHex = 0xFF10B981,
            initials = "JD",
            currentLocation = LocationCoordinate(37.7800, -122.4194, 5f, 1726218000000L),
            isInsideGeofence = true // was inside
        )

        // 1st tick: exits fence -> Alert generated
        val firstResult = checkUseCase(
            groupId = "group_test",
            member = member,
            fence = fence
        )
        assertNotNull(firstResult.generatedAlert, "First exit must generate alert")

        // 2nd tick: still outside -> NO duplicate alert
        val updatedMember = member.copy(
            isInsideGeofence = false,
            currentLocation = LocationCoordinate(37.7805, -122.4194, 5f, 1726218005000L)
        )
        val secondResult = checkUseCase(
            groupId = "group_test",
            member = updatedMember,
            fence = fence
        )
        assertNull(secondResult.generatedAlert, "Second tick while outside must NOT repeat alert")
    }

    @Test
    fun testReturnToGeofenceDoesNotPushAlertAndAllowsReAlertOnSubsequentExit() {
        val checkUseCase = CheckGeofenceBreachUseCase()
        val member = GroupMember(
            id = "m13",
            name = "Jane Doe",
            role = MemberRole.MEMBER,
            avatarColorHex = 0xFF8B5CF6,
            initials = "JD",
            currentLocation = LocationCoordinate(37.7800, -122.4194, 5f, 1726218000000L),
            isInsideGeofence = true
        )

        // 1. Exit fence -> alerts once
        val exitResult = checkUseCase("group_test", member, fence)
        assertNotNull(exitResult.generatedAlert)

        // 2. Return inside fence -> TRANSITION_ENTER, NO breach alert
        val returnedMember = member.copy(
            isInsideGeofence = false,
            currentLocation = LocationCoordinate(37.7749, -122.4194, 5f, 1726218010000L) // center
        )
        val returnResult = checkUseCase("group_test", returnedMember, fence)
        assertTrue(returnResult.isInside)
        assertNull(returnResult.generatedAlert, "Returning to geofence must NOT push an alert")
        assertEquals(com.app.nebulaiqtask.domain.usecase.GeofenceTransition.TRANSITION_ENTER, returnResult.transition)

        // 3. Exit fence AGAIN -> alerts again!
        val reExitMember = member.copy(
            isInsideGeofence = true, // now inside again
            currentLocation = LocationCoordinate(37.7810, -122.4194, 5f, 1726218020000L) // stepped outside again
        )
        val reExitResult = checkUseCase("group_test", reExitMember, fence)
        assertFalse(reExitResult.isInside)
        assertNotNull(reExitResult.generatedAlert, "Exiting again must trigger breach alert once more")
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

    @Test
    fun testOwnerExitDoesNotGenerateAlert() {
        val checkUseCase = CheckGeofenceBreachUseCase()
        val ownerMember = GroupMember(
            id = "leader1",
            name = "Group Owner",
            role = MemberRole.LEADER,
            avatarColorHex = 0xFFFFD700,
            initials = "GO",
            currentLocation = LocationCoordinate(37.7850, -122.4194, 5f, 1726218000000L),
            isInsideGeofence = true // was inside, now outside
        )

        val result = checkUseCase(
            groupId = "group_test",
            member = ownerMember,
            fence = fence,
            totalGroupMembersCount = 10
        )

        assertFalse(result.isInside)
        assertNull(result.generatedAlert, "Owner exiting geofence perimeter must NEVER generate breach alert")
    }
}
