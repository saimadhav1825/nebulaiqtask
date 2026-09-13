package com.app.nebulaiqtask.presentation.feature.groupdetail.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.nebulaiqtask.presentation.feature.groupdetail.effect.GroupDetailEffect
import com.app.nebulaiqtask.presentation.feature.groupdetail.screencontent.GroupDetailScreenContent
import com.app.nebulaiqtask.presentation.feature.groupdetail.viewmodel.GroupDetailViewModel

@Composable
fun GroupDetailScreen(
    viewModel: GroupDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMemberDetail: (String, String) -> Unit,
    onNavigateToAlerts: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GroupDetailEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is GroupDetailEffect.NavigateToMemberDetail -> {
                    onNavigateToMemberDetail(effect.memberId, effect.groupId)
                }
                is GroupDetailEffect.NavigateToAlerts -> {
                    onNavigateToAlerts(effect.groupId)
                }
                is GroupDetailEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    GroupDetailScreenContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}
