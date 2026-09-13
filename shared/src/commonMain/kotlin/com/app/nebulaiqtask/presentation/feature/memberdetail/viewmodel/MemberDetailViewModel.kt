package com.app.nebulaiqtask.presentation.feature.memberdetail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nebulaiqtask.domain.usecase.*
import com.app.nebulaiqtask.presentation.feature.memberdetail.effect.MemberDetailEffect
import com.app.nebulaiqtask.presentation.feature.memberdetail.intent.MemberDetailIntent
import com.app.nebulaiqtask.presentation.feature.memberdetail.state.MemberDetailState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MemberDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val getTrackingGroupUseCase: GetTrackingGroupUseCase,
    private val triggerMemberExitUseCase: TriggerMemberExitUseCase,
    private val triggerMemberReturnUseCase: TriggerMemberReturnUseCase,
    private val checkGeofenceBreachUseCase: CheckGeofenceBreachUseCase,
    private val sendBreachNotificationUseCase: SendBreachNotificationUseCase
) : ViewModel() {

    private val memberId: String = savedStateHandle.get<String>("memberId") ?: "m10"
    private val groupId: String = savedStateHandle.get<String>("groupId") ?: "group_team_alpha"

    private val _state = MutableStateFlow(MemberDetailState(groupId = groupId))
    val state: StateFlow<MemberDetailState> = _state.asStateFlow()

    private val _effect = Channel<MemberDetailEffect>(Channel.BUFFERED)
    val effect: Flow<MemberDetailEffect> = _effect.receiveAsFlow()

    init {
        loadMember()
    }

    private fun loadMember() {
        viewModelScope.launch {
            val group = getTrackingGroupUseCase.getOnce(groupId)
            val member = getGroupMembersUseCase.getMember(memberId)
            _state.update {
                it.copy(
                    isLoading = false,
                    member = member,
                    geofence = group?.geofence
                )
            }
        }
    }

    fun onIntent(intent: MemberDetailIntent) {
        when (intent) {
            is MemberDetailIntent.Refresh -> loadMember()
            is MemberDetailIntent.TriggerBreach -> {
                viewModelScope.launch {
                    val breached = triggerMemberExitUseCase(groupId, memberId)
                    val fence = _state.value.geofence
                    if (breached != null && fence != null) {
                        _state.update { it.copy(member = breached) }
                        val check = checkGeofenceBreachUseCase(groupId, breached.copy(isInsideGeofence = true), fence)
                        if (check.generatedAlert != null) {
                            sendBreachNotificationUseCase(check.generatedAlert, "Field Operations", 9)
                        }
                        _effect.send(MemberDetailEffect.ShowSnackbar("⚠️ Breach event triggered for ${breached.name}!"))
                    }
                }
            }
            is MemberDetailIntent.ReturnToSafety -> {
                viewModelScope.launch {
                    val safe = triggerMemberReturnUseCase(groupId, memberId)
                    if (safe != null) {
                        _state.update { it.copy(member = safe) }
                        sendBreachNotificationUseCase.onMemberReturnedToSafety(groupId, safe.id, safe.name)
                        _effect.send(MemberDetailEffect.ShowSnackbar("✅ ${safe.name} returned inside safe perimeter."))
                    }
                }
            }
            is MemberDetailIntent.SendPingAlert -> {
                viewModelScope.launch {
                    val name = _state.value.member?.name ?: "Member"
                    _effect.send(MemberDetailEffect.ShowSnackbar("📡 Ping sent to $name's device."))
                }
            }
            is MemberDetailIntent.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(MemberDetailEffect.NavigateBack)
                }
            }
        }
    }
}
