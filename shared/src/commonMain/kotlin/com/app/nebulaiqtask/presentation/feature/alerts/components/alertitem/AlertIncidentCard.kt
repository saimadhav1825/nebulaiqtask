package com.app.nebulaiqtask.presentation.feature.alerts.components.alertitem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun AlertIncidentCard(
    alert: BreachAlert,
    onAcknowledge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (!alert.isAcknowledged) Color(0xFFFCA5A5) else NebulaColors.CardBorder,
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (!alert.isAcknowledged) Color(0xFFFEF2F2) else NebulaColors.SurfaceDark
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (!alert.isAcknowledged) androidx.compose.material.icons.Icons.Default.Warning else androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (!alert.isAcknowledged) NebulaColors.CriticalCrimson else NebulaColors.SafeEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!alert.isAcknowledged) "Active Breach" else "Resolved",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!alert.isAcknowledged) NebulaColors.CriticalCrimson else NebulaColors.SafeEmerald
                    )
                }

                Text(
                    text = "${alert.notifiedMembersCount} members alerted",
                    fontSize = 11.sp,
                    color = NebulaColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${alert.memberName} moved ${alert.distanceOutsideMeters.toInt()}m outside perimeter",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextPrimary
            )

            Text(
                text = "Lat: ${alert.breachLocation.latitude.toString().take(7)}, Lon: ${alert.breachLocation.longitude.toString().take(8)}",
                fontSize = 11.sp,
                color = NebulaColors.AccentCyan
            )

            if (!alert.isAcknowledged) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { onAcknowledge(alert.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Acknowledge", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}
