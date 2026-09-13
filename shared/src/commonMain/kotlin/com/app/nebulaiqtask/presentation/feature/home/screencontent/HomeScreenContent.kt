package com.app.nebulaiqtask.presentation.feature.home.screencontent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.presentation.feature.home.components.breachbanner.BreachAlertBanner
import com.app.nebulaiqtask.presentation.feature.home.components.permissionbanner.PermissionRationaleBanner
import com.app.nebulaiqtask.presentation.feature.home.components.quickactions.HomeQuickActions
import com.app.nebulaiqtask.presentation.feature.home.components.radarvisualizer.GeofenceRadarVisualizer
import com.app.nebulaiqtask.presentation.feature.home.intent.HomeIntent
import com.app.nebulaiqtask.presentation.feature.home.state.HomeState
import com.app.nebulaiqtask.presentation.feature.home.state.HomeViewMode
import com.app.nebulaiqtask.presentation.platform.GroupMapView
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebulaColors.DeepBackground
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NebulaColors.PrimaryIndigo)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // 1. Header Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NEBULA IQ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = NebulaColors.PrimaryIndigo
                            )
                            Text(
                                text = "Geofence Sentinel",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebulaColors.TextPrimary
                            )
                        }

                        // Tracking status chip
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (state.isTrackingActive) NebulaColors.SafeEmeraldContainer else NebulaColors.CardElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isTrackingActive) NebulaColors.SafeEmerald else NebulaColors.WarningAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isTrackingActive) "${state.members.size} CONNECTED" else "PAUSED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isTrackingActive) Color.White else NebulaColors.TextSecondary
                            )
                        }
                    }
                }

                // 2. Permission Banner (if any permission is missing)
                if (!state.hasLocationPermission || !state.hasNotificationPermission) {
                    item {
                        PermissionRationaleBanner(
                            hasLocationPermission = state.hasLocationPermission,
                            hasNotificationPermission = state.hasNotificationPermission,
                            onRequestPermissions = { onIntent(HomeIntent.RequestPermissions) }
                        )
                    }
                }

                // 3. Active Group Summary Card or Onboarding Card
                val group = state.activeGroup
                if (group != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onIntent(HomeIntent.OnGroupCardClicked(group.id)) }
                                .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = group.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NebulaColors.TextPrimary
                                        )
                                        Text(
                                            text = "${group.geofence.name} • ${group.geofence.radiusMeters.toInt()}m Radius",
                                            fontSize = 12.sp,
                                            color = NebulaColors.AccentCyan
                                        )
                                    }

                                    // Group Invite Code Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NebulaColors.CardElevated)
                                            .border(1.dp, NebulaColors.PrimaryIndigo, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Code: ${group.id}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NebulaColors.PrimaryIndigo
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "No Active Tracking Group",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NebulaColors.TextPrimary
                                )
                                Text(
                                    text = "Create a new geofence group or join an existing group with a 6-character code.",
                                    fontSize = 12.sp,
                                    color = NebulaColors.TextSecondary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { onIntent(HomeIntent.OnCreateGroupClicked) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("+ Create Group")
                                    }
                                    OutlinedButton(
                                        onClick = { onIntent(HomeIntent.ShowJoinGroupDialog(true)) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("🔗 Join with Code")
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Breach Alert Banner
                item {
                    BreachAlertBanner(
                        alert = state.latestAlert,
                        onAcknowledge = { onIntent(HomeIntent.AcknowledgeAlert(it)) }
                    )
                }

                // 5. Live Geofence Visualizer: Interactive Map vs Radar Canvas
                if (group != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // View Mode Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NebulaColors.SurfaceDark)
                                    .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (state.viewMode == HomeViewMode.MAP) NebulaColors.PrimaryIndigo else Color.Transparent)
                                        .clickable { onIntent(HomeIntent.OnViewModeChanged(HomeViewMode.MAP)) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🗺️ Live Map",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.viewMode == HomeViewMode.MAP) Color.White else NebulaColors.TextSecondary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (state.viewMode == HomeViewMode.RADAR) NebulaColors.PrimaryIndigo else Color.Transparent)
                                        .clickable { onIntent(HomeIntent.OnViewModeChanged(HomeViewMode.RADAR)) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📡 Radar Canvas",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.viewMode == HomeViewMode.RADAR) Color.White else NebulaColors.TextSecondary
                                    )
                                }
                            }

                            if (state.viewMode == HomeViewMode.MAP) {
                                GroupMapView(
                                    geofence = group.geofence,
                                    members = state.members,
                                    onMemberClick = { memberId ->
                                        onIntent(HomeIntent.OnMemberClicked(memberId, group.id))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                )
                            } else {
                                GeofenceRadarVisualizer(
                                    geofence = group.geofence,
                                    members = state.members,
                                    onMemberClicked = { memberId ->
                                        onIntent(HomeIntent.OnMemberClicked(memberId, group.id))
                                    }
                                )
                            }
                        }
                    }

                    // 6. Geofence Control & Multi-Device Panel
                    item {
                        HomeQuickActions(
                            members = state.members,
                            isTrackingActive = state.isTrackingActive,
                            useRealDeviceGps = state.useRealDeviceGps,
                            onToggleTracking = { onIntent(HomeIntent.ToggleTracking) },
                            onToggleRealDeviceGps = { onIntent(HomeIntent.OnToggleRealDeviceGps(it)) },
                            onTriggerBreach = { onIntent(HomeIntent.TriggerBreachForMember(it)) },
                            onReturnToSafety = { onIntent(HomeIntent.ReturnMemberToSafety(it)) },
                            onJoinGroupClicked = { onIntent(HomeIntent.ShowJoinGroupDialog(true)) },
                            onAddMemberClicked = { onIntent(HomeIntent.ShowAddMemberDialog(true)) },
                            onCreateGroupClicked = { onIntent(HomeIntent.OnCreateGroupClicked) },
                            onViewAlertsClicked = {
                                onIntent(HomeIntent.OnAlertsClicked(group.id))
                            }
                        )
                    }

                    // 7. Group Members Live Roster Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL MEMBERS ROSTER (${state.members.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NebulaColors.TextSecondary
                            )

                            Text(
                                text = "Manage →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NebulaColors.PrimaryIndigo,
                                modifier = Modifier.clickable { onIntent(HomeIntent.OnGroupCardClicked(group.id)) }
                            )
                        }
                    }

                    // 8. Member Items
                    items(state.members, key = { it.id }) { member ->
                        MemberRowItem(
                            member = member,
                            deviceBatteryPercent = state.deviceBatteryPercent,
                            onMemberClick = {
                                onIntent(HomeIntent.OnMemberClicked(member.id, group.id))
                            }
                        )
                    }
                }
            }
        }
    }

    // Join Group Dialog
    if (state.isJoinGroupDialogVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(HomeIntent.ShowJoinGroupDialog(false)) },
            containerColor = NebulaColors.SurfaceDark,
            title = {
                Text(
                    text = "🔗 Join Tracking Group",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebulaColors.TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the 6-character group invite code shared by the group creator (e.g. NEB-7492):",
                        fontSize = 12.sp,
                        color = NebulaColors.TextSecondary
                    )
                    OutlinedTextField(
                        value = state.joinGroupCodeInput,
                        onValueChange = { onIntent(HomeIntent.OnJoinGroupCodeChanged(it)) },
                        label = { Text("Group Invite Code") },
                        placeholder = { Text("e.g. NEB-7492") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaColors.PrimaryIndigo,
                            unfocusedBorderColor = NebulaColors.CardBorder,
                            focusedTextColor = NebulaColors.TextPrimary,
                            unfocusedTextColor = NebulaColors.TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onIntent(HomeIntent.SubmitJoinGroup) },
                    enabled = state.joinGroupCodeInput.isNotBlank() && !state.isSubmittingAction,
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo)
                ) {
                    if (state.isSubmittingAction) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Join Group")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(HomeIntent.ShowJoinGroupDialog(false)) }) {
                    Text("Cancel", color = NebulaColors.TextSecondary)
                }
            }
        )
    }

    // Add Member Dialog
    if (state.isAddMemberDialogVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(HomeIntent.ShowAddMemberDialog(false)) },
            containerColor = NebulaColors.SurfaceDark,
            title = {
                Text(
                    text = "👤 Add Member to Group",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebulaColors.TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Register a real member into this active tracking group:",
                        fontSize = 12.sp,
                        color = NebulaColors.TextSecondary
                    )
                    OutlinedTextField(
                        value = state.newMemberNameInput,
                        onValueChange = { onIntent(HomeIntent.OnNewMemberNameChanged(it)) },
                        label = { Text("Member Name") },
                        placeholder = { Text("e.g. Sarah Connor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaColors.PrimaryIndigo,
                            unfocusedBorderColor = NebulaColors.CardBorder,
                            focusedTextColor = NebulaColors.TextPrimary,
                            unfocusedTextColor = NebulaColors.TextPrimary
                        )
                    )

                    Text(
                        text = "Assign Member Role:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebulaColors.TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(MemberRole.MEMBER, MemberRole.SCOUT, MemberRole.OPERATOR).forEach { role ->
                            val isSelected = state.newMemberRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NebulaColors.PrimaryIndigo else NebulaColors.CardElevated)
                                    .border(1.dp, if (isSelected) Color.Transparent else NebulaColors.CardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onIntent(HomeIntent.OnNewMemberRoleChanged(role)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else NebulaColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onIntent(HomeIntent.SubmitAddMember) },
                    enabled = state.newMemberNameInput.isNotBlank() && !state.isSubmittingAction,
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.AccentCyan)
                ) {
                    if (state.isSubmittingAction) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Add Member", color = NebulaColors.DeepBackground, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(HomeIntent.ShowAddMemberDialog(false)) }) {
                    Text("Cancel", color = NebulaColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MemberRowItem(
    member: GroupMember,
    deviceBatteryPercent: Int = 0,
    onMemberClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMemberClick() }
            .border(
                1.dp,
                if (!member.isInsideGeofence) NebulaColors.CriticalCrimson else NebulaColors.CardBorder,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (!member.isInsideGeofence) NebulaColors.CriticalCrimsonContainer.copy(alpha = 0.4f) else NebulaColors.SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(member.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebulaColors.TextPrimary
                    )
                    if (member.isLocalUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(You)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NebulaColors.AccentCyan
                        )
                    }
                }
                val batteryDisplay = if (member.isLocalUser && deviceBatteryPercent > 0) {
                    "🔋 $deviceBatteryPercent% (Hardware)"
                } else {
                    "🔋 ${member.batteryPercent}%"
                }
                Text(
                    text = "Role: ${member.role.name.replace("_", " ")} • $batteryDisplay",
                    fontSize = 11.sp,
                    color = NebulaColors.TextSecondary
                )
            }

            // Status indicator badge
            val isBreached = !member.isInsideGeofence
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isBreached) NebulaColors.CriticalCrimson else NebulaColors.SafeEmeraldContainer
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isBreached) "+${member.distanceToFenceMeters.toInt()}m OUT" else "INSIDE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBreached) Color.White else NebulaColors.SafeEmerald
                )
            }
        }
    }
}

