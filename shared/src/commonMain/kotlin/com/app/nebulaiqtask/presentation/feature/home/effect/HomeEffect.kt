package com.app.nebulaiqtask.presentation.feature.home.effect

sealed interface HomeEffect {
    data class ShowSnackbar(val message: String) : HomeEffect
    data class NavigateToGroupDetail(val groupId: String) : HomeEffect
    data object NavigateToCreateGroup : HomeEffect
    data class NavigateToAlerts(val groupId: String) : HomeEffect
    data class NavigateToMemberDetail(val memberId: String, val groupId: String) : HomeEffect
    data object RequestSystemPermissions : HomeEffect
}
