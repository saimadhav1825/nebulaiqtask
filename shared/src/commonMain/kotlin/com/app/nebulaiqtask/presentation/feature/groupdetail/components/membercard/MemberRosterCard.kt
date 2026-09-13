package com.app.nebulaiqtask.presentation.feature.groupdetail.components.membercard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun MemberRosterCard(
    member: GroupMember,
    onMemberClicked: () -> Unit,
    onTriggerBreach: () -> Unit,
    onReturnToSafety: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBreached = !member.isInsideGeofence
    val isOwner = member.role == com.app.nebulaiqtask.domain.model.MemberRole.LEADER

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onMemberClicked() }
            .border(
                1.dp,
                if (isBreached) Color(0xFFFCA5A5) else NebulaColors.CardBorder,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreached) Color(0xFFFEF2F2) else NebulaColors.SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(member.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NebulaColors.TextPrimary
                        )
                        if (isOwner) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFFBEB))
                                    .border(0.8.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Star,
                                        contentDescription = "Owner",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Owner",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                        if (member.isLocalUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(You)",
                                fontSize = 11.sp,
                                color = NebulaColors.AccentCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "${member.role.name.replace("_", " ")} • Battery: ${member.batteryPercent}%",
                        fontSize = 12.sp,
                        color = NebulaColors.TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isBreached) Color(0xFFFEF2F2) else NebulaColors.SafeEmeraldContainer
                        )
                        .border(
                            1.dp,
                            if (isBreached) Color(0xFFFCA5A5) else Color(0xFFA7F3D0),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBreached) androidx.compose.material.icons.Icons.Default.Warning else androidx.compose.material.icons.Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isBreached) NebulaColors.CriticalCrimson else NebulaColors.SafeEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBreached) "+${member.distanceToFenceMeters.toInt()}m Outside" else "Inside",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBreached) NebulaColors.CriticalCrimson else NebulaColors.SafeEmerald
                        )
                    }
                }
            }

            if (!member.isLocalUser) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBreached) {
                        Button(
                            onClick = onReturnToSafety,
                            colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.SafeEmerald),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Return to Safety", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onTriggerBreach,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NebulaColors.CriticalCrimson),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = NebulaColors.CriticalCrimson,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Exit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
