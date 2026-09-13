package com.app.nebulaiqtask.presentation.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.GroupMember

@Composable
actual fun GroupMapView(
    geofence: GeofenceZone,
    members: List<GroupMember>,
    modifier: Modifier,
    isInteractive: Boolean,
    onMemberClick: (String) -> Unit,
    onMapCenterChange: ((Double, Double) -> Unit)?
) {
    Box(
        modifier = modifier.fillMaxWidth().height(300.dp).background(com.app.nebulaiqtask.presentation.theme.NebulaColors.SurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        Text("Map View (iOS Preview)", color = com.app.nebulaiqtask.presentation.theme.NebulaColors.TextPrimary)
    }
}
