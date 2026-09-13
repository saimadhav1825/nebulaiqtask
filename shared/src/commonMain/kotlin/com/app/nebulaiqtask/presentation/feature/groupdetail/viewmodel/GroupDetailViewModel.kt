package com.app.nebulaiqtask.presentation.feature.groupdetail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nebulaiqtask.domain.usecase.*
import com.app.nebulaiqtask.presentation.feature.groupdetail.effect.GroupDetailEffect
import com.app.nebulaiqtask.presentation.feature.groupdetail.intent.GroupDetailIntent
import com.app.nebulaiqtask.presentation.feature.groupdetail.state.GroupDetailState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getTrackingGroupUseCase: GetTrackingGroupUseCase,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val triggerMemberExitUseCase: TriggerMemberExitUseCase,
    private val triggerMemberReturnUseCase: TriggerMemberReturnUseCase,
    private val checkGeofenceBreachUseCase: CheckGeofenceBreachUseCase,
    private val sendBreachNotificationUseCase: SendBreachNotificationUseCase
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: "group_team_alpha"

    private val _state = MutableStateFlow(GroupDetailState())
    val state: StateFlow<GroupDetailState> = _state.asStateFlow()

    private val _effect = Channel<GroupDetailEffect>(Channel.BUFFERED)
    val effect: Flow<GroupDetailEffect> = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getTrackingGroupUseCase(groupId).collectLatest { grp ->
                _state.update { it.copy(group = grp, isLoading = false) }
            }
        }

        viewModelScope.launch {
            getGroupMembersUseCase(groupId).collectLatest { memberList ->
                _state.update { it.copy(members = memberList) }
            }
        }
    }

    fun onIntent(intent: GroupDetailIntent) {
        when (intent) {
            is GroupDetailIntent.Refresh -> loadData()
            is GroupDetailIntent.OnFilterSelected -> {
                _state.update { it.copy(selectedFilter = intent.filter) }
            }
            is GroupDetailIntent.TriggerBreach -> {
                viewModelScope.launch {
                    val breached = triggerMemberExitUseCase(groupId, intent.memberId)
                    val group = _state.value.group
                    if (breached != null && group != null) {
                        val check = checkGeofenceBreachUseCase(
                            groupId = groupId,
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
                        _effect.send(GroupDetailEffect.ShowSnackbar("${breached.name} exited the geofence perimeter."))
                    }
                }
            }
            is GroupDetailIntent.ReturnToSafety -> {
                viewModelScope.launch {
                    val safe = triggerMemberReturnUseCase(groupId, intent.memberId)
                    if (safe != null) {
                        _effect.send(GroupDetailEffect.ShowSnackbar("${safe.name} is back inside the safe zone."))
                    }
                }
            }
            is GroupDetailIntent.OnMemberClicked -> {
                viewModelScope.launch {
                    _effect.send(GroupDetailEffect.NavigateToMemberDetail(intent.memberId, groupId))
                }
            }
            is GroupDetailIntent.OnViewAlertsClicked -> {
                viewModelScope.launch {
                    _effect.send(GroupDetailEffect.NavigateToAlerts(groupId))
                }
            }
            is GroupDetailIntent.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(GroupDetailEffect.NavigateBack)
                }
            }
        }
    }
}
