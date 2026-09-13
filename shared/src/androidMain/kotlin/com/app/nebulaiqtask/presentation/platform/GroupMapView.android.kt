package com.app.nebulaiqtask.presentation.platform

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
            .background(Color(0xFF0F172A))
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
                    (geofence.radiusMeters / metersPerDp).dp
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
                .background(NebulaColors.SurfaceDark.copy(alpha = 0.92f))
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
                Text(
                    text = "🚨 $breachedMembersCount BREACHED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NebulaColors.CriticalCrimson
                )
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
                        .background(NebulaColors.SurfaceDark.copy(alpha = 0.9f))
                        .border(1.dp, NebulaColors.CardBorder, CircleShape)
                ) {
                    Text("🎯", fontSize = 14.sp)
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
                        .background(NebulaColors.SurfaceDark.copy(alpha = 0.9f))
                        .border(1.dp, NebulaColors.CardBorder, CircleShape)
                ) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                        .background(NebulaColors.SurfaceDark.copy(alpha = 0.9f))
                        .border(1.dp, NebulaColors.CardBorder, CircleShape)
                ) {
                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple expanding halo when member breaches boundary
            if (isBreached) {
                Box(
                    modifier = Modifier
                        .size((28 + 18 * rippleProgress).dp)
                        .border(
                            width = (2 * (1f - rippleProgress)).dp,
                            color = NebulaColors.CriticalCrimson.copy(alpha = 1f - rippleProgress),
                            shape = CircleShape
                        )
                )
            }

            // Member avatar circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isBreached) NebulaColors.CriticalCrimson else Color(member.avatarColorHex)
                    )
                    .border(
                        2.dp,
                        if (member.isLocalUser) NebulaColors.AccentCyan else Color.White,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Member first name label with breach distance
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(NebulaColors.SurfaceDark.copy(alpha = 0.95f))
                .border(
                    0.5.dp,
                    if (isBreached) NebulaColors.CriticalCrimson else NebulaColors.CardBorder,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            val label = if (isBreached) {
                "${member.name.split(" ").first()} (+${member.distanceToFenceMeters.toInt()}m)"
            } else if (member.isLocalUser) {
                "${member.name.split(" ").first()} (You)"
            } else {
                member.name.split(" ").first()
            }
            Text(
                text = label,
                color = if (isBreached) NebulaColors.CriticalCrimson else NebulaColors.TextPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
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
