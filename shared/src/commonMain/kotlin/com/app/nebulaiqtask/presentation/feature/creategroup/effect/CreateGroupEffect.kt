package com.app.nebulaiqtask.presentation.feature.creategroup.effect

sealed interface CreateGroupEffect {
    data class GroupCreated(val groupId: String) : CreateGroupEffect
    data class ShowError(val message: String) : CreateGroupEffect
    data object NavigateBack : CreateGroupEffect
}
