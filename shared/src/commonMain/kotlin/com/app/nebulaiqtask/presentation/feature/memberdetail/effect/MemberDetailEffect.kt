package com.app.nebulaiqtask.presentation.feature.memberdetail.effect

sealed interface MemberDetailEffect {
    data class ShowSnackbar(val message: String) : MemberDetailEffect
    data object NavigateBack : MemberDetailEffect
}
