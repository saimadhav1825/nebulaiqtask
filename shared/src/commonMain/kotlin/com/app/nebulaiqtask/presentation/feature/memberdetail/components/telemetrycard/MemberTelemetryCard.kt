package com.app.nebulaiqtask.presentation.feature.memberdetail.components.telemetrycard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun MemberTelemetryCard(
    member: GroupMember,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "LIVE TELEMETRY & GPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextSecondary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TelemetryItem(label = "Latitude", value = member.currentLocation.latitude.toString().take(9))
                TelemetryItem(label = "Longitude", value = member.currentLocation.longitude.toString().take(10))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TelemetryItem(label = "Battery", value = "${member.batteryPercent}%")
                TelemetryItem(
                    label = "Geofence Status",
                    value = if (member.isInsideGeofence) "Inside Safe Zone" else "BREACHED (+${member.distanceToFenceMeters.toInt()}m)"
                )
            }
        }
    }
}

@Composable
private fun TelemetryItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = NebulaColors.TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NebulaColors.TextPrimary)
    }
}
