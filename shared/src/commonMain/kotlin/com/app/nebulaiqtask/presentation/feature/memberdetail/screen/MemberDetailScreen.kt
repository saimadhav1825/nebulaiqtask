package com.app.nebulaiqtask.presentation.feature.memberdetail.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.nebulaiqtask.presentation.feature.memberdetail.effect.MemberDetailEffect
import com.app.nebulaiqtask.presentation.feature.memberdetail.screencontent.MemberDetailScreenContent
import com.app.nebulaiqtask.presentation.feature.memberdetail.viewmodel.MemberDetailViewModel

@Composable
fun MemberDetailScreen(
    viewModel: MemberDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MemberDetailEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MemberDetailEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    MemberDetailScreenContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}
