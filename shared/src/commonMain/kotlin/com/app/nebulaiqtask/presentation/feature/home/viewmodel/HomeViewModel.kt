package com.app.nebulaiqtask.presentation.feature.home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nebulaiqtask.domain.usecase.*
import com.app.nebulaiqtask.presentation.feature.home.effect.HomeEffect
import com.app.nebulaiqtask.presentation.feature.home.intent.HomeIntent
import com.app.nebulaiqtask.presentation.feature.home.state.HomeState
import com.app.nebulaiqtask.presentation.feature.home.state.HomeViewMode
import com.app.nebulaiqtask.presentation.platform.PlatformDeviceTelemetry
import com.app.nebulaiqtask.presentation.platform.PlatformLocationTracker
import com.app.nebulaiqtask.presentation.platform.PlatformNotificationManager
import com.app.nebulaiqtask.presentation.platform.PlatformPermissionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getTrackingGroupUseCase: GetTrackingGroupUseCase,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val checkGeofenceBreachUseCase: CheckGeofenceBreachUseCase,
    private val simulateMemberMovementUseCase: SimulateMemberMovementUseCase,
    private val sendBreachNotificationUseCase: SendBreachNotificationUseCase,
    private val triggerMemberExitUseCase: TriggerMemberExitUseCase,
    private val triggerMemberReturnUseCase: TriggerMemberReturnUseCase,
    private val acknowledgeAlertUseCase: AcknowledgeAlertUseCase,
    private val toggleTrackingUseCase: ToggleTrackingUseCase,
    private val getActiveAlertsUseCase: GetActiveAlertsUseCase,
    private val updateMemberLocationUseCase: UpdateMemberLocationUseCase,
    private val permissionManager: PlatformPermissionManager,
    private val locationTracker: PlatformLocationTracker,
    private val deviceTelemetry: PlatformDeviceTelemetry,
    private val notificationManager: PlatformNotificationManager
) : ViewModel() {

    private val defaultGroupId = "group_team_alpha"
    private val currentGroupId: String
        get() = savedStateHandle.get<String>("KEY_GROUP_ID") ?: defaultGroupId

    private val _state = MutableStateFlow(
        HomeState(
            isLoading = true,
            hasLocationPermission = permissionManager.hasLocationPermission(),
            hasNotificationPermission = permissionManager.hasNotificationPermission(),
            deviceBatteryPercent = deviceTelemetry.getBatteryPercentage()
        )
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private var simulationJob: Job? = null
    private var gpsTrackingJob: Job? = null

    init {
        savedStateHandle["KEY_GROUP_ID"] = currentGroupId
        observeGroupData()
        startAutomaticSimulation()
        checkPermissions()
    }

    private fun checkPermissions() {
        _state.update {
            it.copy(
                hasLocationPermission = permissionManager.hasLocationPermission(),
                hasNotificationPermission = permissionManager.hasNotificationPermission(),
                deviceBatteryPercent = deviceTelemetry.getBatteryPercentage()
            )
        }
    }

    private fun observeGroupData() {
        viewModelScope.launch {
            getTrackingGroupUseCase(currentGroupId).collectLatest { group ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        activeGroup = group,
                        isTrackingActive = group?.isTrackingActive ?: true
                    )
                }
            }
        }

        viewModelScope.launch {
            getGroupMembersUseCase(currentGroupId).collectLatest { membersList ->
                val group = _state.value.activeGroup
                if (group != null && membersList.isNotEmpty()) {
                    for (member in membersList) {
                        val result = checkGeofenceBreachUseCase(
                            groupId = currentGroupId,
                            member = member,
                            fence = group.geofence,
                            totalGroupMembersCount = membersList.size
                        )

                        // If state changed or alert generated
                        if (result.generatedAlert != null) {
                            sendBreachNotificationUseCase(
                                alert = result.generatedAlert,
                                groupName = group.name,
                                recipientCount = membersList.size - 1
                            )
                            notificationManager.playBreachAlertHapticAndAudio()
                        }
                    }
                }
                _state.update { it.copy(members = membersList) }
            }
        }

        viewModelScope.launch {
            getActiveAlertsUseCase(currentGroupId).collectLatest { alerts ->
                val unacknowledged = alerts.firstOrNull { !it.isAcknowledged }
                _state.update {
                    it.copy(
                        latestAlert = unacknowledged,
                        totalBreachesCount = alerts.size
                    )
                }
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Refresh -> {
                checkPermissions()
                observeGroupData()
            }
            is HomeIntent.OnViewModeChanged -> {
                _state.update { it.copy(viewMode = intent.mode) }
            }
            is HomeIntent.OnToggleRealDeviceGps -> {
                _state.update { it.copy(useRealDeviceGps = intent.enabled) }
                if (intent.enabled) {
                    startRealDeviceGps()
                } else {
                    stopRealDeviceGps()
                }
            }
            is HomeIntent.RequestPermissions -> {
                viewModelScope.launch {
                    _effect.send(HomeEffect.RequestSystemPermissions)
                }
            }
            is HomeIntent.OnPermissionsUpdated -> {
                checkPermissions()
                if (_state.value.useRealDeviceGps && _state.value.hasLocationPermission) {
                    startRealDeviceGps()
                }
            }
            is HomeIntent.ToggleSimulation -> {
                if (_state.value.isSimulationRunning) {
                    stopSimulation()
                } else {
                    startAutomaticSimulation()
                }
            }
            is HomeIntent.ToggleTracking -> {
                val group = _state.value.activeGroup ?: return
                viewModelScope.launch {
                    val updated = toggleTrackingUseCase(group.id, !group.isTrackingActive)
                    _state.update { it.copy(isTrackingActive = updated.isTrackingActive) }
                    _effect.send(HomeEffect.ShowSnackbar("Tracking ${if (updated.isTrackingActive) "Activated" else "Paused"}"))
                }
            }
            is HomeIntent.TriggerBreachForMember -> {
                viewModelScope.launch {
                    val group = _state.value.activeGroup ?: return@launch
                    val breached = triggerMemberExitUseCase(group.id, intent.memberId)
                    if (breached != null) {
                        val check = checkGeofenceBreachUseCase(
                            groupId = group.id,
                            member = breached,
                            fence = group.geofence,
                            totalGroupMembersCount = _state.value.members.size
                        )
                        if (check.generatedAlert != null) {
                            sendBreachNotificationUseCase(
                                alert = check.generatedAlert,
                                groupName = group.name,
                                recipientCount = _state.value.members.size - 1
                            )
                            notificationManager.playBreachAlertHapticAndAudio()
                        }
                        _effect.send(HomeEffect.ShowSnackbar("⚠️ Breach triggered for ${breached.name}! Group alerted."))
                    }
                }
            }
            is HomeIntent.ReturnMemberToSafety -> {
                viewModelScope.launch {
                    val group = _state.value.activeGroup ?: return@launch
                    val safe = triggerMemberReturnUseCase(group.id, intent.memberId)
                    if (safe != null) {
                        _effect.send(HomeEffect.ShowSnackbar("✅ ${safe.name} returned inside safe perimeter."))
                    }
                }
            }
            is HomeIntent.AcknowledgeAlert -> {
                viewModelScope.launch {
                    acknowledgeAlertUseCase(intent.alertId)
                    _effect.send(HomeEffect.ShowSnackbar("Alert acknowledged."))
                }
            }
            is HomeIntent.OnCreateGroupClicked -> {
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToCreateGroup)
                }
            }
            is HomeIntent.OnGroupCardClicked -> {
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToGroupDetail(intent.groupId))
                }
            }
            is HomeIntent.OnAlertsClicked -> {
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToAlerts(intent.groupId))
                }
            }
            is HomeIntent.OnMemberClicked -> {
                viewModelScope.launch {
                    _effect.send(HomeEffect.NavigateToMemberDetail(intent.memberId, intent.groupId))
                }
            }
        }
    }

    private fun startRealDeviceGps() {
        gpsTrackingJob?.cancel()
        gpsTrackingJob = viewModelScope.launch {
            locationTracker.startLocationUpdates().collectLatest { realCoord ->
                val group = _state.value.activeGroup
                val localMember = _state.value.members.find { it.isLocalUser }
                if (group != null && localMember != null) {
                    val check = checkGeofenceBreachUseCase(
                        groupId = group.id,
                        member = localMember.copy(currentLocation = realCoord),
                        fence = group.geofence,
                        totalGroupMembersCount = _state.value.members.size
                    )

                    updateMemberLocationUseCase(
                        memberId = localMember.id,
                        location = realCoord,
                        isInside = check.isInside,
                        distanceToFence = check.distanceOutsideMeters
                    )

                    if (check.generatedAlert != null) {
                        sendBreachNotificationUseCase(
                            alert = check.generatedAlert,
                            groupName = group.name,
                            recipientCount = _state.value.members.size - 1
                        )
                        notificationManager.playBreachAlertHapticAndAudio()
                    }
                }
            }
        }
    }

    private fun stopRealDeviceGps() {
        gpsTrackingJob?.cancel()
        gpsTrackingJob = null
        locationTracker.stopLocationUpdates()
    }

    private fun startAutomaticSimulation() {
        simulationJob?.cancel()
        _state.update { it.copy(isSimulationRunning = true) }
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(3500)
                if (_state.value.isTrackingActive) {
                    simulateMemberMovementUseCase(currentGroupId)
                }
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        _state.update { it.copy(isSimulationRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        stopRealDeviceGps()
    }
}
