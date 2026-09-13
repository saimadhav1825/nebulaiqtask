package com.app.nebulaiqtask.presentation.feature.home.components.quickactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun HomeQuickActions(
    members: List<GroupMember>,
    isTrackingActive: Boolean,
    useRealDeviceGps: Boolean,
    onToggleTracking: () -> Unit,
    onToggleRealDeviceGps: (Boolean) -> Unit,
    onTriggerBreach: (String) -> Unit,
    onReturnToSafety: (String) -> Unit,
    onJoinGroupClicked: () -> Unit,
    onAddMemberClicked: () -> Unit,
    onCreateGroupClicked: () -> Unit,
    onViewAlertsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMemberMenu by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull { !it.isLocalUser }?.id ?: "") }

    val selectedMember = members.find { it.id == selectedMemberId } ?: members.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
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
                Text(
                    text = "Geofence & Device Controls",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NebulaColors.TextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isTrackingActive) "Active" else "Paused",
                        fontSize = 12.sp,
                        color = if (isTrackingActive) NebulaColors.SafeEmerald else NebulaColors.WarningAmber,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isTrackingActive,
                        onCheckedChange = { onToggleTracking() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NebulaColors.SafeEmerald,
                            uncheckedThumbColor = NebulaColors.TextSecondary,
                            uncheckedTrackColor = NebulaColors.CardElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real Device GPS Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NebulaColors.CardElevated)
                    .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = NebulaColors.PrimaryIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Device GPS Hardware", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NebulaColors.TextPrimary)
                        if (useRealDeviceGps) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NebulaColors.AccentCyan)
                        }
                    }
                    Text(
                        text = if (useRealDeviceGps) "Streaming coordinates to group" else "GPS streaming paused",
                        fontSize = 11.sp,
                        color = NebulaColors.TextSecondary
                    )
                }
                Switch(
                    checked = useRealDeviceGps,
                    onCheckedChange = { onToggleRealDeviceGps(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NebulaColors.AccentCyan,
                        uncheckedThumbColor = NebulaColors.TextSecondary,
                        uncheckedTrackColor = NebulaColors.DeepBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Join Group & Add Member Quick Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onJoinGroupClicked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Join Group", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onAddMemberClicked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NebulaColors.PrimaryIndigo),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NebulaColors.CardBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = null,
                        tint = NebulaColors.PrimaryIndigo,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Member", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (members.any { !it.isLocalUser }) {
                Spacer(modifier = Modifier.height(14.dp))

                // Member Selector Bar for breach alert test
                Text(
                    text = "Simulate Geofence Breach:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NebulaColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NebulaColors.CardElevated)
                        .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(10.dp))
                        .clickable { expandedMemberMenu = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMember?.let { "${it.name} (${if (it.isInsideGeofence) "Inside" else "Outside"})" }
                                ?: "Select member",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedMember?.isInsideGeofence == false) NebulaColors.CriticalCrimson else NebulaColors.TextPrimary
                        )
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = NebulaColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMemberMenu,
                        onDismissRequest = { expandedMemberMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        members.filter { !it.isLocalUser }.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${m.name} - ${if (m.isInsideGeofence) "Inside" else "Outside"}",
                                        color = if (!m.isInsideGeofence) NebulaColors.CriticalCrimson else NebulaColors.TextPrimary
                                    )
                                },
                                onClick = {
                                    selectedMemberId = m.id
                                    expandedMemberMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons: Breach vs Return to Safety
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            selectedMember?.let { onTriggerBreach(it.id) }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.CriticalCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Exit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            selectedMember?.let { onReturnToSafety(it.id) }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.SafeEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Return Safe", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Row: Alerts Log & New Group
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onViewAlertsClicked,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NebulaColors.CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NebulaColors.TextPrimary)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                        contentDescription = null,
                        tint = NebulaColors.TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Incident Log", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onCreateGroupClicked,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Group", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

