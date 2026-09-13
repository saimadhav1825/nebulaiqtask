package com.app.nebulaiqtask.presentation.feature.creategroup.components.presetpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun GeofencePresetPicker(
    selectedIndex: Int,
    onSelectPreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        "🎯 Test (25m)",
        "🏢 Campus (250m)",
        "🌲 Park (600m)",
        "🏫 School (150m)"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Geofence Template Presets",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NebulaColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NebulaColors.PrimaryIndigo else NebulaColors.CardElevated)
                        .border(
                            1.dp,
                            if (isSelected) NebulaColors.PrimaryIndigo else NebulaColors.CardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectPreset(index) }
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) NebulaColors.TextPrimary else NebulaColors.TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
