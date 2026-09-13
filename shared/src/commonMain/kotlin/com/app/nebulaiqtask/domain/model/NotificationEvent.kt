package com.app.nebulaiqtask.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationEvent(
    val id: String,
    val alertId: String,
    val recipientMemberId: String,
    val recipientName: String,
    val message: String,
    val deliveredTimestamp: Long,
    val isDelivered: Boolean = true
)
