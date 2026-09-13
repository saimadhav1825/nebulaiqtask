package com.app.nebulaiqtask.presentation.feature.home.components.quickactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
                    text = "GEOFENCE & MULTI-DEVICE CONTROLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebulaColors.TextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isTrackingActive) "Active" else "Paused",
                        fontSize = 11.sp,
                        color = if (isTrackingActive) NebulaColors.SafeEmerald else NebulaColors.WarningAmber,
                        fontWeight = FontWeight.Bold
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
                        Text("📍 Hardware GPS Chip", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NebulaColors.TextPrimary)
                        if (useRealDeviceGps) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE SYNC", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = NebulaColors.AccentCyan)
                        }
                    }
                    Text(
                        text = if (useRealDeviceGps) "Streaming your real physical GPS to Firebase" else "GPS streaming paused",
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
                    Text("🔗 Join Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddMemberClicked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.AccentCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Add Member", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NebulaColors.DeepBackground)
                }
            }

            if (members.any { !it.isLocalUser }) {
                Spacer(modifier = Modifier.height(14.dp))

                // Member Selector Bar for breach alert test
                Text(
                    text = "Test Geofence Breach on Member:",
                    fontSize = 12.sp,
                    color = NebulaColors.TextPrimary
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
                            text = selectedMember?.let { "${it.name} (${if (it.isInsideGeofence) "Inside" else "OUTSIDE"})" }
                                ?: "Select member",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedMember?.isInsideGeofence == false) NebulaColors.CriticalCrimson else NebulaColors.TextPrimary
                        )
                        Text("▼", fontSize = 11.sp, color = NebulaColors.TextSecondary)
                    }

                    DropdownMenu(
                        expanded = expandedMemberMenu,
                        onDismissRequest = { expandedMemberMenu = false },
                        modifier = Modifier.background(NebulaColors.SurfaceDark)
                    ) {
                        members.filter { !it.isLocalUser }.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${m.name} - ${if (m.isInsideGeofence) "Inside" else "OUTSIDE"}",
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
                        Text("🚨 Test Exit Breach", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            selectedMember?.let { onReturnToSafety(it.id) }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.SafeEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🛡️ Return Safe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NebulaColors.AccentCyan)
                ) {
                    Text("📋 Incident Log", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onCreateGroupClicked,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo)
                ) {
                    Text("+ New Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

