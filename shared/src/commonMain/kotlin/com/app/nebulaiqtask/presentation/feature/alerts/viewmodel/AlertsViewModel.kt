package com.app.nebulaiqtask.presentation.feature.alerts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nebulaiqtask.domain.usecase.AcknowledgeAlertUseCase
import com.app.nebulaiqtask.domain.usecase.ClearActiveAlertsUseCase
import com.app.nebulaiqtask.domain.usecase.GetActiveAlertsUseCase
import com.app.nebulaiqtask.domain.usecase.GetDeliveredNotificationsUseCase
import com.app.nebulaiqtask.presentation.feature.alerts.effect.AlertsEffect
import com.app.nebulaiqtask.presentation.feature.alerts.intent.AlertsIntent
import com.app.nebulaiqtask.presentation.feature.alerts.state.AlertsState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getActiveAlertsUseCase: GetActiveAlertsUseCase,
    private val acknowledgeAlertUseCase: AcknowledgeAlertUseCase,
    private val getDeliveredNotificationsUseCase: GetDeliveredNotificationsUseCase,
    private val clearActiveAlertsUseCase: ClearActiveAlertsUseCase
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: "group_team_alpha"

    private val _state = MutableStateFlow(AlertsState(groupId = groupId))
    val state: StateFlow<AlertsState> = _state.asStateFlow()

    private val _effect = Channel<AlertsEffect>(Channel.BUFFERED)
    val effect: Flow<AlertsEffect> = _effect.receiveAsFlow()

    init {
        loadAlertsAndNotifications()
    }

    private fun loadAlertsAndNotifications() {
        viewModelScope.launch {
            getActiveAlertsUseCase(groupId).collectLatest { alertsList ->
                _state.update { it.copy(alerts = alertsList) }
            }
        }

        viewModelScope.launch {
            getDeliveredNotificationsUseCase().collectLatest { notificationsList ->
                _state.update { it.copy(notifications = notificationsList) }
            }
        }
    }

    fun onIntent(intent: AlertsIntent) {
        when (intent) {
            is AlertsIntent.Refresh -> loadAlertsAndNotifications()
            is AlertsIntent.OnTabSelected -> {
                _state.update { it.copy(selectedTab = intent.tab) }
            }
            is AlertsIntent.AcknowledgeAlert -> {
                viewModelScope.launch {
                    acknowledgeAlertUseCase(intent.alertId)
                    _effect.send(AlertsEffect.ShowSnackbar("Alert acknowledged."))
                }
            }
            is AlertsIntent.ClearAllAlerts -> {
                viewModelScope.launch {
                    clearActiveAlertsUseCase(groupId)
                    _effect.send(AlertsEffect.ShowSnackbar("All alerts cleared."))
                }
            }
            is AlertsIntent.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(AlertsEffect.NavigateBack)
                }
            }
        }
    }
}
