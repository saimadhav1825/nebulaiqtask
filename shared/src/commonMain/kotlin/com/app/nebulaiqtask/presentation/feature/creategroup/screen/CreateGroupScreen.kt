package com.app.nebulaiqtask.presentation.feature.creategroup.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.nebulaiqtask.presentation.feature.creategroup.effect.CreateGroupEffect
import com.app.nebulaiqtask.presentation.feature.creategroup.screencontent.CreateGroupScreenContent
import com.app.nebulaiqtask.presentation.feature.creategroup.viewmodel.CreateGroupViewModel

@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel,
    onNavigateBack: () -> Unit,
    onGroupCreated: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateGroupEffect.GroupCreated -> {
                    onGroupCreated(effect.groupId)
                }
                is CreateGroupEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CreateGroupEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    CreateGroupScreenContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}
