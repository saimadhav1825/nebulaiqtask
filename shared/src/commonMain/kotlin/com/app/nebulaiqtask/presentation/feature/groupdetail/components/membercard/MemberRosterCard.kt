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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onMemberClicked() }
            .border(
                1.dp,
                if (isBreached) NebulaColors.CriticalCrimson else NebulaColors.CardBorder,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreached) NebulaColors.CriticalCrimsonContainer.copy(alpha = 0.35f) else NebulaColors.SurfaceDark
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
                            if (isBreached) NebulaColors.CriticalCrimson else NebulaColors.SafeEmeraldContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isBreached) "BREACH (+${member.distanceToFenceMeters.toInt()}m)" else "INSIDE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBreached) Color.White else NebulaColors.SafeEmerald
                    )
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
                            Text("🛡️ Return to Safety", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onTriggerBreach,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NebulaColors.CriticalCrimson),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("🚨 Simulate Exit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
