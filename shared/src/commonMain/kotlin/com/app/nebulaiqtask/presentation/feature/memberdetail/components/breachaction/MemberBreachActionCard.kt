package com.app.nebulaiqtask.presentation.feature.memberdetail.components.breachaction

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
                text = "GEOFENCE BREACH & PING ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextSecondary
            )

            if (!member.isInsideGeofence) {
                Button(
                    onClick = onReturnToSafety,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.SafeEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🛡️ Return Member to Safety Zone", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onTriggerBreach,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.CriticalCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🚨 Simulate Geofence Breach (Exit)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            OutlinedButton(
                onClick = onSendPing,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NebulaColors.CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NebulaColors.AccentCyan)
            ) {
                Text("📡 Ping Member Device", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}
