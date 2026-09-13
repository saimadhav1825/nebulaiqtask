package com.app.nebulaiqtask.presentation.platform

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.presentation.theme.NebulaColors
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.AndroidRenderMode
import org.maplibre.compose.map.MapUiOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.map.renderMode
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

@Composable
actual fun GroupMapView(
    geofence: GeofenceZone,
    members: List<GroupMember>,
    modifier: Modifier,
    isInteractive: Boolean,
    onMemberClick: (String) -> Unit,
    onMapCenterChange: ((Double, Double) -> Unit)?
) {
    val coroutineScope = rememberCoroutineScope()

    val initialCameraPosition = remember(geofence.center) {
        CameraPosition(
            target = Position(
                longitude = geofence.center.longitude,
                latitude = geofence.center.latitude
            ),
            zoom = 15.0
        )
    }

    val mapState = rememberMapState(
        baseStyle = BaseStyle.Demo,
        initialCameraPosition = initialCameraPosition
    )

    // Keep camera in sync when geofence center changes from presets or external inputs
    LaunchedEffect(geofence.center.latitude, geofence.center.longitude) {
        mapState.setCameraPosition(
            mapState.cameraPosition.copy(
                target = Position(
                    longitude = geofence.center.longitude,
                    latitude = geofence.center.latitude
                )
            )
        )
    }

    // Interaction bindings: Tap-to-move geofence center (e.g. in Create Group)
    val interactions = remember(isInteractive, onMapCenterChange) {
        if (!isInteractive) {
            MapInteractions {
                camera {
                    pan { enabled = false }
                    zoom { enabled = false }
                    rotate { enabled = false }
                    tilt { enabled = false }
                }
            }
        } else {
            MapInteractions {
                if (onMapCenterChange != null) {
                    callbacks {
                        click {
                            onEvent { event ->
                                event.position?.let { pos ->
                                    onMapCenterChange(pos.latitude, pos.longitude)
                                }
                                ClickResult.Pass
                            }
                        }
                    }
                }
            }
        }
    }

    // Alert pulse ripple animation for breached members
    val infiniteTransition = rememberInfiniteTransition(label = "map_breach_ripple")
    val alertRippleProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_progress"
    )

    val breachedMembersCount = members.count { !it.isInsideGeofence }

    val uiOptions = remember {
        MapUiOptions {
            renderMode = AndroidRenderMode.Texture
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NebulaColors.SurfaceDark)
            .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(20.dp))
    ) {
        // MapLibre Interactive Native Map View
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            interactions = interactions,
            uiOptions = uiOptions,
            overlay = {
                // 1. Geofence Perimeter Radius Circle
                val metersPerDp = mapState.viewport?.metersPerDpAtTarget ?: 1.0
                val radiusDp = if (metersPerDp > 0.0) {
                    ((geofence.radiusMeters / metersPerDp).dp).coerceAtLeast(18.dp)
                } else {
                    90.dp
                }

                Box(
                    modifier = Modifier
                        .placedAt(
                            position = Position(
                                longitude = geofence.center.longitude,
                                latitude = geofence.center.latitude
                            ),
                            alignment = Alignment.Center
                        )
                        .size(radiusDp * 2)
                        .border(2.dp, NebulaColors.AccentCyan, CircleShape)
                        .background(NebulaColors.AccentCyan.copy(alpha = 0.12f), CircleShape)
                )

                // 2. Geofence Center Anchor
                Box(
                    modifier = Modifier
                        .placedAt(
                            position = Position(
                                longitude = geofence.center.longitude,
                                latitude = geofence.center.latitude
                            ),
                            alignment = Alignment.Center
                        )
                        .size(16.dp)
                        .border(2.dp, Color.White, CircleShape)
                        .background(NebulaColors.PrimaryIndigo, CircleShape)
                )

                // 3. Render Group Members with Live Geodesic Positioning & Visual Overlap Dispersal
                val dispersedMembers = remember(members) {
                    disperseOverlappingMembers(members)
                }

                dispersedMembers.forEach { (member, displayCoord) ->
                    key(member.id) {
                        MemberMapPin(
                            member = member,
                            isBreached = !member.isInsideGeofence,
                            rippleProgress = alertRippleProgress,
                            onClick = { onMemberClick(member.id) },
                            modifier = Modifier.placedAt(
                                position = Position(
                                    longitude = displayCoord.longitude,
                                    latitude = displayCoord.latitude
                                ),
                                alignment = Alignment.Center
                            )
                        )
                    }
                }
            }
        )

        // Top Geofence Status Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NebulaColors.SurfaceDark.copy(alpha = 0.95f))
                .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (breachedMembersCount > 0) NebulaColors.CriticalCrimson else NebulaColors.SafeEmerald)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${geofence.name} • ${geofence.radiusMeters.toInt()}m",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextPrimary
            )
            if (breachedMembersCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                        contentDescription = null,
                        tint = NebulaColors.CriticalCrimson,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$breachedMembersCount Outside",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebulaColors.CriticalCrimson
                    )
                }
            }
        }

        // Map Control Floating Action Buttons (Recenter, Zoom In, Zoom Out)
        if (isInteractive) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Recenter Camera on Geofence Center
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            mapState.setCameraPosition(
                                CameraPosition(
                                    target = Position(
                                        longitude = geofence.center.longitude,
                                        latitude = geofence.center.latitude
                                    ),
                                    zoom = 15.0
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NebulaColors.SurfaceDark.copy(alpha = 0.95f))
                        .border(1.dp, NebulaColors.CardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.LocationOn,
                        contentDescription = "Recenter",
                        tint = NebulaColors.PrimaryIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom In
                IconButton(
                    onClick = {
                        val current = mapState.cameraPosition
                        mapState.setCameraPosition(current.copy(zoom = (current.zoom + 1.0).coerceAtMost(22.0)))
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NebulaColors.SurfaceDark.copy(alpha = 0.95f))
                        .border(1.dp, NebulaColors.CardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = "Zoom in",
                        tint = NebulaColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom Out
                IconButton(
                    onClick = {
                        val current = mapState.cameraPosition
                        mapState.setCameraPosition(current.copy(zoom = (current.zoom - 1.0).coerceAtLeast(1.0)))
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NebulaColors.SurfaceDark.copy(alpha = 0.95f))
                        .border(1.dp, NebulaColors.CardBorder, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.dp)
                            .background(NebulaColors.TextPrimary, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberMapPin(
    member: GroupMember,
    isBreached: Boolean,
    rippleProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOwner = member.role == MemberRole.LEADER
    val firstName = member.name.split(" ").firstOrNull().orEmpty().ifBlank { member.name }

    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple expanding halo when member breaches boundary
            if (isBreached) {
                Box(
                    modifier = Modifier
                        .size((28 + 20 * rippleProgress).dp)
                        .border(
                            width = (2 * (1f - rippleProgress)).dp,
                            color = NebulaColors.CriticalCrimson.copy(alpha = 1f - rippleProgress),
                            shape = CircleShape
                        )
                )
            }

            // Member avatar circle
            val borderColor = when {
                isBreached -> NebulaColors.CriticalCrimson
                isOwner -> Color(0xFFF59E0B) // Amber for Owner/Leader
                member.isLocalUser -> NebulaColors.AccentCyan
                else -> Color.White
            }
            val borderWidth = if (isOwner) 2.5.dp else 2.dp

            Box(
                modifier = Modifier
                    .size(if (isOwner) 32.dp else 28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isBreached) NebulaColors.CriticalCrimson else Color(member.avatarColorHex)
                    )
                    .border(borderWidth, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isOwner) 12.sp else 11.sp
                )
            }

            // Owner Star Badge anchored on top-right corner
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFF59E0B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = "Owner",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        // Member label badge with Owner / You / Breach indicators
        val badgeBackground = when {
            isBreached -> Color(0xFFFEF2F2)
            isOwner -> Color(0xFFFFFBEB) // Warm amber container for Owner
            member.isLocalUser -> Color(0xFFEFF6FF) // Soft indigo/blue for You
            else -> Color(0xFFFFFFFF)
        }
        val badgeBorderColor = when {
            isBreached -> Color(0xFFFCA5A5)
            isOwner -> Color(0xFFFDE68A)
            member.isLocalUser -> Color(0xFFBFDBFE)
            else -> NebulaColors.CardBorder
        }
        val labelText = when {
            isBreached -> "$firstName (+${member.distanceToFenceMeters.toInt()}m)"
            isOwner && member.isLocalUser -> "$firstName (Owner • You)"
            isOwner -> "$firstName (Owner)"
            member.isLocalUser -> "$firstName (You)"
            else -> firstName
        }
        val textColor = when {
            isBreached -> Color(0xFFDC2626)
            isOwner -> Color(0xFFB45309)
            member.isLocalUser -> Color(0xFF1D4ED8)
            else -> NebulaColors.TextPrimary
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBackground)
                .border(0.8.dp, badgeBorderColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOwner) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                } else if (isBreached) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = labelText,
                    color = textColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Ensures co-located or overlapping members (such as newly joined members prior to moving)
 * have slightly offset visual pin positions so all members remain clearly visible and clickable.
 */
internal fun disperseOverlappingMembers(members: List<GroupMember>): List<Pair<GroupMember, LocationCoordinate>> {
    if (members.isEmpty()) return emptyList()

    val result = mutableListOf<Pair<GroupMember, LocationCoordinate>>()
    val clusters = mutableListOf<MutableList<GroupMember>>()

    for (member in members) {
        val cluster = clusters.find { cl ->
            val first = cl.first().currentLocation
            val dLat = member.currentLocation.latitude - first.latitude
            val dLon = member.currentLocation.longitude - first.longitude
            // ~15 meters squared in degrees squared
            (dLat * dLat + dLon * dLon) < 0.00000003
        }
        if (cluster != null) {
            cluster.add(member)
        } else {
            clusters.add(mutableListOf(member))
        }
    }

    for (cluster in clusters) {
        if (cluster.size == 1) {
            result.add(cluster[0] to cluster[0].currentLocation)
        } else {
            val count = cluster.size
            for (i in cluster.indices) {
                val member = cluster[i]
                val angle = (2.0 * kotlin.math.PI * i / count)
                val baseLat = member.currentLocation.latitude
                val baseLon = member.currentLocation.longitude
                val radiusMeters = 20.0 // 20 meters spacing between overlapping pins
                val latOffset = (radiusMeters / 111000.0) * kotlin.math.cos(angle)
                val lonOffset = (radiusMeters / (111000.0 * kotlin.math.cos(baseLat * kotlin.math.PI / 180.0))) * kotlin.math.sin(angle)
                val dispersed = member.currentLocation.copy(
                    latitude = baseLat + latOffset,
                    longitude = baseLon + lonOffset
                )
                result.add(member to dispersed)
            }
        }
    }

    return result
}
