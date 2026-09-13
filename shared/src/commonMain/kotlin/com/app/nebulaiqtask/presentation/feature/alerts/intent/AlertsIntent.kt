package com.app.nebulaiqtask.presentation.feature.alerts.intent

import com.app.nebulaiqtask.presentation.feature.alerts.state.AlertsTab

sealed interface AlertsIntent {
    data object Refresh : AlertsIntent
    data class OnTabSelected(val tab: AlertsTab) : AlertsIntent
    data class AcknowledgeAlert(val alertId: String) : AlertsIntent
    data object ClearAllAlerts : AlertsIntent
    data object OnBackClicked : AlertsIntent
}
