package com.app.nebulaiqtask.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.GroupMember

@Composable
expect fun GroupMapView(
    geofence: GeofenceZone,
    members: List<GroupMember>,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onMemberClick: (String) -> Unit = {},
    onMapCenterChange: ((Double, Double) -> Unit)? = null
)
