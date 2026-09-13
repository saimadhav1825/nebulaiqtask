package com.app.nebulaiqtask.presentation.feature.creategroup.state

data class CreateGroupState(
    val groupName: String = "Expedition Team",
    val geofenceName: String = "Test 1m Geofence",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val radiusMeters: Double = 1.0,
    val alertOnExit: Boolean = true,
    val alertOnEntry: Boolean = false,
    val isSubmitting: Boolean = false,
    val selectedPresetIndex: Int = 0
)
