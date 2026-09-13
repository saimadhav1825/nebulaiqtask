package com.app.nebulaiqtask.presentation.feature.creategroup.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.usecase.CreateTrackingGroupUseCase
import com.app.nebulaiqtask.presentation.feature.creategroup.effect.CreateGroupEffect
import com.app.nebulaiqtask.presentation.feature.creategroup.intent.CreateGroupIntent
import com.app.nebulaiqtask.presentation.feature.creategroup.state.CreateGroupState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock as DateTimeClock

class CreateGroupViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val createTrackingGroupUseCase: CreateTrackingGroupUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        CreateGroupState(
            groupName = savedStateHandle["SAVED_GROUP_NAME"] ?: "",
            geofenceName = savedStateHandle["SAVED_GEOFENCE_NAME"] ?: "",
            radiusMeters = savedStateHandle["SAVED_RADIUS"] ?: 400.0
        )
    )
    val state: StateFlow<CreateGroupState> = _state.asStateFlow()

    private val _effect = Channel<CreateGroupEffect>(Channel.BUFFERED)
    val effect: Flow<CreateGroupEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: CreateGroupIntent) {
        when (intent) {
            is CreateGroupIntent.OnGroupNameChanged -> {
                savedStateHandle["SAVED_GROUP_NAME"] = intent.name
                _state.update { it.copy(groupName = intent.name) }
            }
            is CreateGroupIntent.OnGeofenceNameChanged -> {
                savedStateHandle["SAVED_GEOFENCE_NAME"] = intent.name
                _state.update { it.copy(geofenceName = intent.name) }
            }
            is CreateGroupIntent.OnRadiusChanged -> {
                savedStateHandle["SAVED_RADIUS"] = intent.radius
                _state.update { it.copy(radiusMeters = intent.radius) }
            }
            is CreateGroupIntent.OnCoordinatesChanged -> {
                _state.update { it.copy(latitude = intent.lat, longitude = intent.lon) }
            }
            is CreateGroupIntent.OnPresetSelected -> {
                val presets = listOf(
                    Triple("Corporate Campus", 250.0, 37.7749 to -122.4194),
                    Triple("National Park Camp", 600.0, 37.7690 to -122.4467),
                    Triple("Urban Event Zone", 400.0, 37.7858 to -122.4065),
                    Triple("High-School Perimeter", 150.0, 37.7550 to -122.4200)
                )
                val preset = presets.getOrNull(intent.index)
                if (preset != null) {
                    _state.update {
                        it.copy(
                            selectedPresetIndex = intent.index,
                            geofenceName = preset.first,
                            radiusMeters = preset.second,
                            latitude = preset.third.first,
                            longitude = preset.third.second
                        )
                    }
                }
            }
            is CreateGroupIntent.OnAlertOnExitToggled -> {
                _state.update { it.copy(alertOnExit = intent.enabled) }
            }
            is CreateGroupIntent.OnCreateClicked -> {
                createGroup()
            }
            is CreateGroupIntent.OnBackClicked -> {
                viewModelScope.launch { _effect.send(CreateGroupEffect.NavigateBack) }
            }
        }
    }

    private fun createGroup() {
        val s = _state.value
        if (s.groupName.isBlank()) {
            viewModelScope.launch { _effect.send(CreateGroupEffect.ShowError("Please enter a group name")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            try {
                val now = DateTimeClock.System.now().toEpochMilliseconds()
                val geofence = GeofenceZone(
                    id = "fence_${now}",
                    name = s.geofenceName,
                    center = LocationCoordinate(s.latitude, s.longitude, 3.5f, now),
                    radiusMeters = s.radiusMeters,
                    description = "Custom geofence configured with ${s.radiusMeters.toInt()}m boundary",
                    alertOnExit = s.alertOnExit,
                    createdAt = now
                )

                val created = createTrackingGroupUseCase(
                    name = s.groupName,
                    geofence = geofence
                )

                _effect.send(CreateGroupEffect.GroupCreated(created.id))
            } catch (e: Exception) {
                _effect.send(CreateGroupEffect.ShowError(e.message ?: "Failed to create group"))
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
