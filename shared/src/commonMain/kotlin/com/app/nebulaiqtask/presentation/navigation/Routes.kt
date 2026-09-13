package com.app.nebulaiqtask.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class GroupDetailRoute(
    val groupId: String
)

@Serializable
object CreateGroupRoute

@Serializable
data class MemberDetailRoute(
    val memberId: String,
    val groupId: String
)

@Serializable
data class AlertsRoute(
    val groupId: String
)
