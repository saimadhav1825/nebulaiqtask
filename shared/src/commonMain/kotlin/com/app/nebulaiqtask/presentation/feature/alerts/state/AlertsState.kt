package com.app.nebulaiqtask.presentation.feature.alerts.state

import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.domain.model.NotificationEvent

data class AlertsState(
    val isLoading: Boolean = false,
    val alerts: List<BreachAlert> = emptyList(),
    val notifications: List<NotificationEvent> = emptyList(),
    val groupId: String = "group_team_alpha",
    val selectedTab: AlertsTab = AlertsTab.ACTIVE_BREACHES
)

enum class AlertsTab {
    ACTIVE_BREACHES,
    DELIVERED_NOTIFICATIONS
}
