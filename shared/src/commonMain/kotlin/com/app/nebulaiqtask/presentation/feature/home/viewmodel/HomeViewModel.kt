package com.app.nebulaiqtask.presentation.feature.home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nebulaiqtask.domain.repository.UserRepository
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val getTrackingGroupUseCase: GetTrackingGroupUseCase,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val checkGeofenceBreachUseCase: CheckGeofenceBreachUseCase,
    private val joinTrackingGroupUseCase: JoinTrackingGroupUseCase,
    private val addGroupMemberUseCase: AddGroupMemberUseCase,
    private val removeGroupMemberUseCase: RemoveGroupMemberUseCase,
    private val sendBreachNotificationUseCase: SendBreachNotificationUseCase,
    private val triggerMemberExitUseCase: TriggerMemberExitUseCase,
    private val triggerMemberReturnUseCase: TriggerMemberReturnUseCase,
    private val acknowledgeAlertUseCase: AcknowledgeAlertUseCase,
    private val toggleTrackingUseCase: ToggleTrackingUseCase,
    private val getActiveAlertsUseCase: GetActiveAlertsUseCase,
    private val updateMemberLocationUseCase: UpdateMemberLocationUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val initializeUserSessionUseCase: InitializeUserSessionUseCase,
    private val updateDisplayNameUseCase: UpdateDisplayNameUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val permissionManager: PlatformPermissionManager,
    private val locationTracker: PlatformLocationTracker,
    private val deviceTelemetry: PlatformDeviceTelemetry,
    private val notificationManager: PlatformNotificationManager
) : ViewModel() {

    // Active group ID — null means no group joined/created yet (empty state)
    private var activeGroupId: String?
        get() = savedStateHandle.get<String>("KEY_GROUP_ID")
        set(value) {
            savedStateHandle["KEY_GROUP_ID"] = value
        }

    private val _state = MutableStateFlow(
        HomeState(
            isLoading = true,
            hasLocationPermission = permissionManager.hasLocationPermission(),
            hasNotificationPermission = permissionManager.hasNotificationPermission(),
            deviceBatteryPercent = deviceTelemetry.getBatteryPercentage(),
            useRealDeviceGps = true,
            currentUserId = "",
            currentUserName = ""
        )
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private var groupObservationJob: Job? = null
    private var membersObservationJob: Job? = null
    private var alertsObservationJob: Job? = null
    private var gpsTrackingJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                initializeUserSessionUseCase()
            } catch (e: Exception) {
                // Initial session error handled gracefully
            }
        }
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { profile ->
                _state.update {
                    it.copy(
                        currentUserId = profile.userId,
                        currentUserName = profile.displayName
                    )
                }
            }
        }

        // Observe group data: restore from savedStateHandle or persistent UserRepository
        viewModelScope.launch {
            val currentId = activeGroupId
            if (currentId != null) {
                observeGroupData()
            } else {
                val savedId = userRepository.getActiveGroupId()
                if (!savedId.isNullOrBlank()) {
                    activeGroupId = savedId
                    _state.update { it.copy(isLoading = true) }
                    observeGroupData()
                } else {
                    // No group yet — show empty state UI
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
        checkPermissions()
        if (_state.value.hasLocationPermission) {
            startRealDeviceGps()
        }
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
        groupObservationJob?.cancel()
        membersObservationJob?.cancel()
        alertsObservationJob?.cancel()

        val groupId = activeGroupId ?: return  // No group — nothing to observe

        groupObservationJob = viewModelScope.launch {
            getTrackingGroupUseCase(groupId).collectLatest { group ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        activeGroup = group,
                        isTrackingActive = group?.isTrackingActive ?: true
                    )
                }
            }
        }

        membersObservationJob = viewModelScope.launch {
            getGroupMembersUseCase(groupId).collectLatest { membersList ->
                val group = _state.value.activeGroup
                if (group != null && membersList.isNotEmpty()) {
                    for (member in membersList) {
                        val result = checkGeofenceBreachUseCase(
                            groupId = groupId,
                            member = member,
                            fence = group.geofence,
                            totalGroupMembersCount = membersList.size
                        )

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

        alertsObservationJob = viewModelScope.launch {
            getActiveAlertsUseCase(groupId).collectLatest { alerts ->
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
                // Kept for backward compatibility
            }
            is HomeIntent.ToggleTracking -> {
                val group = _state.value.activeGroup ?: return
                viewModelScope.launch {
                    val updated = toggleTrackingUseCase(group.id, !group.isTrackingActive)
                    _state.update { it.copy(isTrackingActive = updated.isTrackingActive) }
                    _effect.send(HomeEffect.ShowSnackbar("Tracking ${if (updated.isTrackingActive) "Active" else "Paused"}"))
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
                            notificationManager.showHeadsUpBreachNotification(
                                title = "🚨 GEOFENCE BREACH: ${breached.name}",
                                message = "${breached.name} stepped outside ${group.geofence.name}!",
                                breachDistanceMeters = check.distanceOutsideMeters,
                                memberName = breached.name
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
            is HomeIntent.SwitchToGroup -> {
                activeGroupId = intent.groupId
                viewModelScope.launch { userRepository.saveActiveGroupId(intent.groupId) }
                _state.update { it.copy(isLoading = true) }
                observeGroupData()
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
            is HomeIntent.ShowJoinGroupDialog -> {
                _state.update { it.copy(isJoinGroupDialogVisible = intent.show, joinGroupCodeInput = "") }
            }
            is HomeIntent.OnJoinGroupCodeChanged -> {
                _state.update { it.copy(joinGroupCodeInput = intent.code.uppercase()) }
            }
            is HomeIntent.SubmitJoinGroup -> {
                joinGroup()
            }
            is HomeIntent.ShowAddMemberDialog -> {
                _state.update { it.copy(isAddMemberDialogVisible = intent.show, newMemberNameInput = "") }
            }
            is HomeIntent.OnNewMemberNameChanged -> {
                _state.update { it.copy(newMemberNameInput = intent.name) }
            }
            is HomeIntent.OnNewMemberRoleChanged -> {
                _state.update { it.copy(newMemberRole = intent.role) }
            }
            is HomeIntent.SubmitAddMember -> {
                addNewMember()
            }
            is HomeIntent.RemoveMember -> {
                viewModelScope.launch {
                    val group = _state.value.activeGroup ?: return@launch
                    removeGroupMemberUseCase(group.id, intent.memberId)
                    _effect.send(HomeEffect.ShowSnackbar("Member removed from group."))
                }
            }
        }
    }

    private fun joinGroup() {
        val code = _state.value.joinGroupCodeInput.trim().uppercase()
        if (code.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmittingAction = true) }
            val result = joinTrackingGroupUseCase(code)
            _state.update { it.copy(isSubmittingAction = false) }

            result.onSuccess { joinedGroup ->
                activeGroupId = joinedGroup.id
                viewModelScope.launch { userRepository.saveActiveGroupId(joinedGroup.id) }
                _state.update {
                    it.copy(
                        isJoinGroupDialogVisible = false,
                        activeGroup = joinedGroup,
                        members = joinedGroup.members
                    )
                }
                observeGroupData()
                _effect.send(HomeEffect.ShowSnackbar("🎉 Successfully joined group ${joinedGroup.name} ($code)"))
            }.onFailure { error ->
                _effect.send(HomeEffect.ShowSnackbar("❌ Failed to join group: ${error.message ?: "Invalid code"}"))
            }
        }
    }

    private fun addNewMember() {
        val name = _state.value.newMemberNameInput.trim()
        val role = _state.value.newMemberRole
        val group = _state.value.activeGroup ?: return

        if (name.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmittingAction = true) }
            val result = addGroupMemberUseCase(group.id, name, role)
            _state.update { it.copy(isSubmittingAction = false, isAddMemberDialogVisible = false) }

            result.onSuccess { newMember ->
                _effect.send(HomeEffect.ShowSnackbar("✅ Added ${newMember.name} to the group!"))
            }.onFailure { error ->
                _effect.send(HomeEffect.ShowSnackbar("❌ Failed to add member: ${error.message}"))
            }
        }
    }

    private fun startRealDeviceGps() {
        gpsTrackingJob?.cancel()
        gpsTrackingJob = viewModelScope.launch {
            locationTracker.startLocationUpdates().collectLatest { realCoord ->
                val group = _state.value.activeGroup
                val myUserId = _state.value.currentUserId
                val battery = deviceTelemetry.getBatteryPercentage()

                if (group != null) {
                    val localMember = _state.value.members.find { it.isLocalUser || it.id == myUserId }
                    if (localMember != null) {
                        val check = checkGeofenceBreachUseCase(
                            groupId = group.id,
                            member = localMember.copy(currentLocation = realCoord),
                            fence = group.geofence,
                            totalGroupMembersCount = _state.value.members.size
                        )

                        updateMemberLocationUseCase(
                            groupId = group.id,
                            memberId = localMember.id,
                            location = realCoord,
                            battery = battery,
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
    }

    private fun stopRealDeviceGps() {
        gpsTrackingJob?.cancel()
        gpsTrackingJob = null
        locationTracker.stopLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        stopRealDeviceGps()
    }
}

