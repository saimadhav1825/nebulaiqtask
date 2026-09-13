package com.app.nebulaiqtask.presentation.feature.alerts.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.nebulaiqtask.presentation.feature.alerts.effect.AlertsEffect
import com.app.nebulaiqtask.presentation.feature.alerts.screencontent.AlertsScreenContent
import com.app.nebulaiqtask.presentation.feature.alerts.viewmodel.AlertsViewModel

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AlertsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AlertsEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    AlertsScreenContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}
