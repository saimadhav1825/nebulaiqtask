package com.app.nebulaiqtask.presentation.feature.groupdetail.effect

sealed interface GroupDetailEffect {
    data class ShowSnackbar(val message: String) : GroupDetailEffect
    data class NavigateToMemberDetail(val memberId: String, val groupId: String) : GroupDetailEffect
    data class NavigateToAlerts(val groupId: String) : GroupDetailEffect
    data object NavigateBack : GroupDetailEffect
}
