package com.app.nebulaiqtask.data.datasource

import com.app.nebulaiqtask.data.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalGroupDataSource {
    private val _groups = MutableStateFlow<Map<String, GroupDto>>(emptyMap())
    val groups: StateFlow<Map<String, GroupDto>> = _groups.asStateFlow()

    private val _alerts = MutableStateFlow<List<BreachAlertDto>>(emptyList())
    val alerts: StateFlow<List<BreachAlertDto>> = _alerts.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationEventDto>>(emptyList())
    val notifications: StateFlow<List<NotificationEventDto>> = _notifications.asStateFlow()

    private val _userLiveLocation = MutableStateFlow<LocationDto?>(null)
    val userLiveLocation: StateFlow<LocationDto?> = _userLiveLocation.asStateFlow()

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
        groupId: String,
        memberId: String,
        location: LocationDto,
        battery: Int,
        isInside: Boolean,
        distanceToFence: Double
    ) {
        _groups.update { current ->
            val group = current[groupId] ?: return@update current
            val updatedMembers = group.members.map { m ->
                if (m.id == memberId) {
                    m.copy(
                        currentLocation = location,
                        batteryPercent = battery,
                        isInsideGeofence = isInside,
                        distanceToFenceMeters = distanceToFence,
                        lastUpdatedMillis = location.timestamp
                    )
                } else m
            }
            current + (groupId to group.copy(members = updatedMembers))
        }
    }

    fun addMember(groupId: String, member: MemberDto) {
        _groups.update { current ->
            val group = current[groupId] ?: return@update current
            val filtered = group.members.filterNot { it.id == member.id }
            current + (groupId to group.copy(members = filtered + member))
        }
    }

    fun removeMember(groupId: String, memberId: String) {
        _groups.update { current ->
            val group = current[groupId] ?: return@update current
            val filtered = group.members.filterNot { it.id == memberId }
            current + (groupId to group.copy(members = filtered))
        }
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
