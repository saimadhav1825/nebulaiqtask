package com.app.nebulaiqtask.presentation.feature.memberdetail.components.breachaction

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun MemberBreachActionCard(
    member: GroupMember,
    onTriggerBreach: () -> Unit,
    onReturnToSafety: () -> Unit,
    onSendPing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Member Actions",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NebulaColors.TextPrimary
            )

            if (!member.isInsideGeofence) {
                Button(
                    onClick = onReturnToSafety,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.SafeEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Return to Safe Zone", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onTriggerBreach,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.CriticalCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulate Geofence Exit", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            OutlinedButton(
                onClick = onSendPing,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NebulaColors.CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NebulaColors.AccentCyan)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Send,
                    contentDescription = null,
                    tint = NebulaColors.AccentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ping Member Device", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}
