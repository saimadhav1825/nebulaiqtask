package com.app.nebulaiqtask.presentation.feature.alerts.effect

sealed interface AlertsEffect {
    data class ShowSnackbar(val message: String) : AlertsEffect
    data object NavigateBack : AlertsEffect
}
