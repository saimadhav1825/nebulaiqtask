package com.app.nebulaiqtask.presentation.feature.home.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nebulaiqtask.domain.usecase.*
import com.app.nebulaiqtask.presentation.feature.home.effect.HomeEffect
import com.app.nebulaiqtask.presentation.feature.home.intent.HomeIntent
import com.app.nebulaiqtask.presentation.feature.home.state.HomeState
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
    private val getActiveAlertsUseCase: GetActiveAlertsUseCase
) : ViewModel() {

    private val defaultGroupId = "group_team_alpha"
    private val currentGroupId: String
        get() = savedStateHandle.get<String>("KEY_GROUP_ID") ?: defaultGroupId

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private var simulationJob: Job? = null

    init {
        savedStateHandle["KEY_GROUP_ID"] = currentGroupId
        observeGroupData()
        startAutomaticSimulation()
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
                    // Check breaches for all members
                    for (member in membersList) {
                        val result = checkGeofenceBreachUseCase(
                            groupId = currentGroupId,
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
                observeGroupData()
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
                        }
                        _effect.send(HomeEffect.ShowSnackbar("⚠️ Breach triggered for ${breached.name}! Group notified."))
                    }
                }
            }
            is HomeIntent.ReturnMemberToSafety -> {
                viewModelScope.launch {
                    val group = _state.value.activeGroup ?: return@launch
                    val safe = triggerMemberReturnUseCase(group.id, intent.memberId)
                    if (safe != null) {
                        _effect.send(HomeEffect.ShowSnackbar("✅ ${safe.name} returned safely inside the geofence."))
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
    }
}
