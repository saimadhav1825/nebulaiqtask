package com.app.nebulaiqtask.presentation.feature.groupdetail.intent

import com.app.nebulaiqtask.presentation.feature.groupdetail.state.MemberFilter

sealed interface GroupDetailIntent {
    data object Refresh : GroupDetailIntent
    data class OnFilterSelected(val filter: MemberFilter) : GroupDetailIntent
    data class TriggerBreach(val memberId: String) : GroupDetailIntent
    data class ReturnToSafety(val memberId: String) : GroupDetailIntent
    data class OnMemberClicked(val memberId: String) : GroupDetailIntent
    data object OnViewAlertsClicked : GroupDetailIntent
    data object OnBackClicked : GroupDetailIntent
}
