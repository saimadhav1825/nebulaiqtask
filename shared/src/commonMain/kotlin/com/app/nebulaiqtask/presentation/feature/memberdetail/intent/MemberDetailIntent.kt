package com.app.nebulaiqtask.presentation.feature.memberdetail.intent

sealed interface MemberDetailIntent {
    data object Refresh : MemberDetailIntent
    data object TriggerBreach : MemberDetailIntent
    data object ReturnToSafety : MemberDetailIntent
    data object SendPingAlert : MemberDetailIntent
    data object OnBackClicked : MemberDetailIntent
}
