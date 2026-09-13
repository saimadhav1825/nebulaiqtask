package com.app.nebulaiqtask.presentation.feature.groupdetail.components.geofencecard

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
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun GeofenceZoneCard(
    geofence: GeofenceZone,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ASSOCIATED GEOFENCE PERIMETER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = geofence.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Center: ${geofence.center.latitude.toString().take(7)}, ${geofence.center.longitude.toString().take(8)} • Radius: ${geofence.radiusMeters.toInt()}m",
                fontSize = 12.sp,
                color = NebulaColors.AccentCyan
            )
            if (geofence.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = geofence.description,
                    fontSize = 11.sp,
                    color = NebulaColors.TextSecondary
                )
            }
        }
    }
}
