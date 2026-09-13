package com.app.nebulaiqtask.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.nebulaiqtask.presentation.feature.alerts.screen.AlertsScreen
import com.app.nebulaiqtask.presentation.feature.alerts.viewmodel.AlertsViewModel
import com.app.nebulaiqtask.presentation.feature.creategroup.screen.CreateGroupScreen
import com.app.nebulaiqtask.presentation.feature.creategroup.viewmodel.CreateGroupViewModel
import com.app.nebulaiqtask.presentation.feature.groupdetail.screen.GroupDetailScreen
import com.app.nebulaiqtask.presentation.feature.groupdetail.viewmodel.GroupDetailViewModel
import com.app.nebulaiqtask.presentation.feature.home.screen.HomeScreen
import com.app.nebulaiqtask.presentation.feature.home.viewmodel.HomeViewModel
import com.app.nebulaiqtask.presentation.feature.memberdetail.screen.MemberDetailScreen
import com.app.nebulaiqtask.presentation.feature.memberdetail.viewmodel.MemberDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NebulaNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            val viewModel = koinViewModel<HomeViewModel>()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(GroupDetailRoute(groupId))
                },
                onNavigateToCreateGroup = {
                    navController.navigate(CreateGroupRoute)
                },
                onNavigateToAlerts = { groupId ->
                    navController.navigate(AlertsRoute(groupId))
                },
                onNavigateToMemberDetail = { memberId, groupId ->
                    navController.navigate(MemberDetailRoute(memberId, groupId))
                }
            )
        }

        composable<GroupDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<GroupDetailRoute>()
            val handle = SavedStateHandle(mapOf("groupId" to route.groupId))
            val viewModel = koinViewModel<GroupDetailViewModel> { parametersOf(handle) }

            GroupDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMemberDetail = { memberId, grpId ->
                    navController.navigate(MemberDetailRoute(memberId, grpId))
                },
                onNavigateToAlerts = { grpId ->
                    navController.navigate(AlertsRoute(grpId))
                }
            )
        }

        composable<CreateGroupRoute> {
            val viewModel = koinViewModel<CreateGroupViewModel>()
            CreateGroupScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onGroupCreated = { groupId ->
                    navController.navigate(GroupDetailRoute(groupId)) {
                        popUpTo<HomeRoute> { inclusive = false }
                    }
                }
            )
        }

        composable<MemberDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<MemberDetailRoute>()
            val handle = SavedStateHandle(mapOf("memberId" to route.memberId, "groupId" to route.groupId))
            val viewModel = koinViewModel<MemberDetailViewModel> { parametersOf(handle) }

            MemberDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AlertsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AlertsRoute>()
            val handle = SavedStateHandle(mapOf("groupId" to route.groupId))
            val viewModel = koinViewModel<AlertsViewModel> { parametersOf(handle) }

            AlertsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
