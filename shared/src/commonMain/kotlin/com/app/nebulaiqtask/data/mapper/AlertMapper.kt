package com.app.nebulaiqtask.data.mapper

import com.app.nebulaiqtask.data.dto.BreachAlertDto
import com.app.nebulaiqtask.data.dto.NotificationEventDto
import com.app.nebulaiqtask.domain.model.AlertSeverity
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.NotificationEvent

class AlertMapper(
    private val locationMapper: LocationMapper
) {
    fun toDomain(dto: BreachAlertDto): BreachAlert {
        return BreachAlert(
            id = dto.id,
            groupId = dto.groupId,
            memberId = dto.memberId,
            memberName = dto.memberName,
            timestamp = dto.timestamp,
            breachLocation = locationMapper.toDomain(dto.breachLocation),
            distanceOutsideMeters = dto.distanceOutsideMeters,
            severity = try {
                AlertSeverity.valueOf(dto.severity)
            } catch (e: Exception) {
                AlertSeverity.CRITICAL
            },
            isAcknowledged = dto.isAcknowledged,
            notifiedMembersCount = dto.notifiedMembersCount
        )
    }

    fun toDto(domain: BreachAlert): BreachAlertDto {
        return BreachAlertDto(
            id = domain.id,
            groupId = domain.groupId,
            memberId = domain.memberId,
            memberName = domain.memberName,
            timestamp = domain.timestamp,
            breachLocation = locationMapper.toDto(domain.breachLocation),
            distanceOutsideMeters = domain.distanceOutsideMeters,
            severity = domain.severity.name,
            isAcknowledged = domain.isAcknowledged,
            notifiedMembersCount = domain.notifiedMembersCount
        )
    }
}

class NotificationEventMapper {
    fun toDomain(dto: NotificationEventDto): NotificationEvent {
        return NotificationEvent(
            id = dto.id,
            alertId = dto.alertId,
            recipientMemberId = dto.recipientMemberId,
            recipientName = dto.recipientName,
            message = dto.message,
            deliveredTimestamp = dto.deliveredTimestamp,
            isDelivered = dto.isDelivered
        )
    }

    fun toDto(domain: NotificationEvent): NotificationEventDto {
        return NotificationEventDto(
            id = domain.id,
            alertId = domain.alertId,
            recipientMemberId = domain.recipientMemberId,
            recipientName = domain.recipientName,
            message = domain.message,
            deliveredTimestamp = domain.deliveredTimestamp,
            isDelivered = domain.isDelivered
        )
    }
}
