package com.app.nebulaiqtask.presentation.feature.creategroup.components.radiusslider

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun GeofenceRadiusSlider(
    radiusMeters: Double,
    onRadiusChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Geofence Radius",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NebulaColors.TextPrimary
            )
            Text(
                text = "${radiusMeters.toInt()} meters",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.PrimaryIndigo
            )
        }

        Slider(
            value = radiusMeters.toFloat(),
            onValueChange = { onRadiusChanged(it.toDouble()) },
            valueRange = 1f..2000f,
            colors = SliderDefaults.colors(
                thumbColor = NebulaColors.PrimaryIndigo,
                activeTrackColor = NebulaColors.PrimaryIndigo,
                inactiveTrackColor = NebulaColors.CardBorder
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("25m (Default)", fontSize = 10.sp, color = NebulaColors.TextSecondary)
            Text("500m (Campus)", fontSize = 10.sp, color = NebulaColors.TextSecondary)
            Text("2000m (District)", fontSize = 10.sp, color = NebulaColors.TextSecondary)
        }
    }
}
