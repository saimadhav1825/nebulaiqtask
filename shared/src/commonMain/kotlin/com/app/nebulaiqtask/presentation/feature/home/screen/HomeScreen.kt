package com.app.nebulaiqtask.presentation.feature.home.screen

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.nebulaiqtask.presentation.feature.home.effect.HomeEffect
import com.app.nebulaiqtask.presentation.feature.home.screencontent.HomeScreenContent
import com.app.nebulaiqtask.presentation.feature.home.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToAlerts: (String) -> Unit,
    onNavigateToMemberDetail: (String, String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is HomeEffect.NavigateToGroupDetail -> {
                    onNavigateToGroupDetail(effect.groupId)
                }
                is HomeEffect.NavigateToCreateGroup -> {
                    onNavigateToCreateGroup()
                }
                is HomeEffect.NavigateToAlerts -> {
                    onNavigateToAlerts(effect.groupId)
                }
                is HomeEffect.NavigateToMemberDetail -> {
                    onNavigateToMemberDetail(effect.memberId, effect.groupId)
                }
                is HomeEffect.RequestSystemPermissions -> {
                    snackbarHostState.showSnackbar("Opening Settings to grant Location & Notification permissions")
                }
            }
        }
    }

    HomeScreenContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}
