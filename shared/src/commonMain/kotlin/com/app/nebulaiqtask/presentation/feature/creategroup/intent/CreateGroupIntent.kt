package com.app.nebulaiqtask.presentation.feature.creategroup.intent

sealed interface CreateGroupIntent {
    data class OnGroupNameChanged(val name: String) : CreateGroupIntent
    data class OnGeofenceNameChanged(val name: String) : CreateGroupIntent
    data class OnRadiusChanged(val radius: Double) : CreateGroupIntent
    data class OnCoordinatesChanged(val lat: Double, val lon: Double) : CreateGroupIntent
    data class OnPresetSelected(val index: Int) : CreateGroupIntent
    data class OnAlertOnExitToggled(val enabled: Boolean) : CreateGroupIntent
    data object OnCreateClicked : CreateGroupIntent
    data object OnBackClicked : CreateGroupIntent
}
