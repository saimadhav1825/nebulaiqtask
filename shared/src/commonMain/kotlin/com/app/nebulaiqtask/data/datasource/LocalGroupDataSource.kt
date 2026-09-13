package com.app.nebulaiqtask.data.datasource

import com.app.nebulaiqtask.data.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalGroupDataSource(
    private val simulatedMembersDataSource: SimulatedMembersDataSource
) {
    private val _groups = MutableStateFlow<Map<String, GroupDto>>(emptyMap())
    val groups: StateFlow<Map<String, GroupDto>> = _groups.asStateFlow()

    private val _alerts = MutableStateFlow<List<BreachAlertDto>>(emptyList())
    val alerts: StateFlow<List<BreachAlertDto>> = _alerts.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationEventDto>>(emptyList())
    val notifications: StateFlow<List<NotificationEventDto>> = _notifications.asStateFlow()

    private val _userLiveLocation = MutableStateFlow<LocationDto?>(null)
    val userLiveLocation: StateFlow<LocationDto?> = _userLiveLocation.asStateFlow()

    private var simulationTickCount = 0L

    init {
        seedDefaultGroup()
    }

    private fun seedDefaultGroup() {
        val defaultCenterLat = 37.7749
        val defaultCenterLon = -122.4194
        val defaultRadius = 300.0 // 300 meters

        val defaultGeofence = GeofenceZoneDto(
            id = "fence_campus_01",
            name = "Mission Bay Campus Safety Zone",
            center = LocationDto(defaultCenterLat, defaultCenterLon, 3.5f, 1726218000000L),
            radiusMeters = defaultRadius,
            description = "Central campus perimeter & geofenced safety boundary",
            alertOnExit = true,
            alertOnEntry = false,
            createdAt = 1726218000000L
        )

        val members = simulatedMembersDataSource.createInitial10Members(
            centerLat = defaultCenterLat,
            centerLon = defaultCenterLon,
            radiusMeters = defaultRadius
        )

        val defaultGroup = GroupDto(
            id = "group_team_alpha",
            name = "Alpha Field Operations (10 Members)",
            geofence = defaultGeofence,
            members = members,
            activeAlertsCount = 0,
            isTrackingActive = true,
            createdAt = 1726218000000L
        )

        _groups.value = mapOf(defaultGroup.id to defaultGroup)
    }

    fun getGroup(groupId: String): GroupDto? = _groups.value[groupId]

    fun saveGroup(group: GroupDto) {
        _groups.update { current -> current + (group.id to group) }
    }

    fun updateMembers(groupId: String, members: List<MemberDto>) {
        _groups.update { current ->
            val existing = current[groupId] ?: return@update current
            current + (groupId to existing.copy(members = members))
        }
    }

    fun updateSingleMember(
        memberId: String,
        location: LocationDto,
        isInside: Boolean,
        distanceToFence: Double
    ) {
        _groups.update { current ->
            current.mapValues { (_, group) ->
                val updatedMembers = group.members.map { m ->
                    if (m.id == memberId) {
                        m.copy(
                            currentLocation = location,
                            isInsideGeofence = isInside,
                            distanceToFenceMeters = distanceToFence,
                            lastUpdatedMillis = location.timestamp
                        )
                    } else m
                }
                group.copy(members = updatedMembers)
            }
        }
    }

    fun simulateTick(groupId: String): List<MemberDto> {
        simulationTickCount++
        val group = _groups.value[groupId] ?: return emptyList()
        val updated = simulatedMembersDataSource.stepSimulation(
            members = group.members,
            centerLat = group.geofence.center.latitude,
            centerLon = group.geofence.center.longitude,
            radiusMeters = group.geofence.radiusMeters,
            tickCount = simulationTickCount
        )
        updateMembers(groupId, updated)
        return updated
    }

    fun triggerMemberExit(groupId: String, memberId: String): MemberDto? {
        val group = _groups.value[groupId] ?: return null
        val target = group.members.find { it.id == memberId } ?: return null
        val breached = simulatedMembersDataSource.forceBreachMember(
            member = target,
            centerLat = group.geofence.center.latitude,
            centerLon = group.geofence.center.longitude,
            radiusMeters = group.geofence.radiusMeters
        )
        val updatedMembers = group.members.map { if (it.id == memberId) breached else it }
        updateMembers(groupId, updatedMembers)
        return breached
    }

    fun triggerMemberReturn(groupId: String, memberId: String): MemberDto? {
        val group = _groups.value[groupId] ?: return null
        val target = group.members.find { it.id == memberId } ?: return null
        val safe = simulatedMembersDataSource.returnMemberToSafety(
            member = target,
            centerLat = group.geofence.center.latitude,
            centerLon = group.geofence.center.longitude,
            radiusMeters = group.geofence.radiusMeters
        )
        val updatedMembers = group.members.map { if (it.id == memberId) safe else it }
        updateMembers(groupId, updatedMembers)
        return safe
    }

    fun addAlert(alert: BreachAlertDto) {
        _alerts.update { current -> listOf(alert) + current }
        _groups.update { current ->
            val group = current[alert.groupId] ?: return@update current
            current + (alert.groupId to group.copy(activeAlertsCount = group.activeAlertsCount + 1))
        }
    }

    fun acknowledgeAlert(alertId: String) {
        _alerts.update { current ->
            current.map { if (it.id == alertId) it.copy(isAcknowledged = true) else it }
        }
    }

    fun clearAlerts(groupId: String) {
        _alerts.update { current -> current.filter { it.groupId != groupId } }
        _groups.update { current ->
            val group = current[groupId] ?: return@update current
            current + (groupId to group.copy(activeAlertsCount = 0))
        }
    }

    fun addNotifications(events: List<NotificationEventDto>) {
        _notifications.update { current -> events + current }
    }

    fun setUserLiveLocation(location: LocationDto) {
        _userLiveLocation.value = location
    }
}
