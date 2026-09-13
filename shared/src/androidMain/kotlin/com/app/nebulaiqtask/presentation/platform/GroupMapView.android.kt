package com.app.nebulaiqtask.presentation.platform

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.GeofenceZone
import com.app.nebulaiqtask.domain.model.GroupMember
import com.app.nebulaiqtask.presentation.theme.NebulaColors
import kotlin.math.cos

@Composable
actual fun GroupMapView(
    geofence: GeofenceZone,
    members: List<GroupMember>,
    modifier: Modifier,
    isInteractive: Boolean,
    onMemberClick: (String) -> Unit,
    onMapCenterChange: ((Double, Double) -> Unit)?
) {
    var zoomScale by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition()
    val alertRippleProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (isInteractive) {
            zoomScale = (zoomScale * zoomChange).coerceIn(0.6f, 3.5f)
            panOffset += offsetChange
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(members, geofence, zoomScale, panOffset) {
                    detectTapGestures { tapOffset ->
                        val minDim = minOf(size.width, size.height).toFloat()
                        val centerX = (size.width / 2f) + panOffset.x
                        val centerY = (size.height / 2f) + panOffset.y
                        val baseRadiusPx = (minDim / 2f) * 0.65f * zoomScale

                        val centerLat = geofence.center.latitude
                        val centerLon = geofence.center.longitude
                        val metersToLat = 1.0 / 111000.0
                        val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))
                        val scale = baseRadiusPx / geofence.radiusMeters.toFloat()

                        // Check if tapped on a member pin
                        var memberTapped = false
                        for (member in members) {
                            val dLat = (member.currentLocation.latitude - centerLat) / metersToLat
                            val dLon = (member.currentLocation.longitude - centerLon) / metersToLon
                            val dotX = centerX + (dLon.toFloat() * scale)
                            val dotY = centerY - (dLat.toFloat() * scale)

                            val dx = tapOffset.x - dotX
                            val dy = tapOffset.y - dotY
                            if (dx * dx + dy * dy < 900f) {
                                onMemberClick(member.id)
                                memberTapped = true
                                break
                            }
                        }

                        // If not tapped on member and center change is allowed, drop pin
                        if (!memberTapped && onMapCenterChange != null) {
                            val clickOffsetX = tapOffset.x - centerX
                            val clickOffsetY = centerY - tapOffset.y

                            val dLonMeters = clickOffsetX / scale
                            val dLatMeters = clickOffsetY / scale

                            val newLat = centerLat + (dLatMeters * metersToLat)
                            val newLon = centerLon + (dLonMeters * metersToLon)
                            onMapCenterChange(newLat, newLon)
                        }
                    }
                }
        ) {
            val minDim = minOf(size.width, size.height)
            val centerX = (size.width / 2f) + panOffset.x
            val centerY = (size.height / 2f) + panOffset.y
            val baseRadiusPx = (minDim / 2f) * 0.65f * zoomScale

            val centerLat = geofence.center.latitude
            val centerLon = geofence.center.longitude
            val metersToLat = 1.0 / 111000.0
            val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))
            val scale = baseRadiusPx / geofence.radiusMeters.toFloat()

            // 1. Stylized Map Grid & Terrain Lines
            val gridStep = 50f * zoomScale
            var gx = centerX % gridStep
            while (gx < size.width) {
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.3f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, size.height),
                    strokeWidth = 1f
                )
                gx += gridStep
            }

            var gy = centerY % gridStep
            while (gy < size.height) {
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.3f),
                    start = Offset(0f, gy),
                    end = Offset(size.width, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // 2. Geofence Safe Perimeter Circle
            // Inner safe translucent fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NebulaColors.SafeEmerald.copy(alpha = 0.12f),
                        NebulaColors.PrimaryIndigo.copy(alpha = 0.05f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = baseRadiusPx
                ),
                radius = baseRadiusPx,
                center = Offset(centerX, centerY)
            )

            // Geofence Boundary Line
            drawCircle(
                color = NebulaColors.PrimaryIndigo,
                radius = baseRadiusPx,
                center = Offset(centerX, centerY),
                style = Stroke(
                    width = 3f * zoomScale.coerceAtMost(2f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                )
            )

            // Center Pin Marker
            drawCircle(
                color = Color.White,
                radius = 7f * zoomScale.coerceIn(0.8f, 1.5f),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = NebulaColors.PrimaryIndigo,
                radius = 4f * zoomScale.coerceIn(0.8f, 1.5f),
                center = Offset(centerX, centerY)
            )

            // 3. Render 10 Group Member Pins
            for (member in members) {
                val dLat = (member.currentLocation.latitude - centerLat) / metersToLat
                val dLon = (member.currentLocation.longitude - centerLon) / metersToLon
                val dotX = centerX + (dLon.toFloat() * scale)
                val dotY = centerY - (dLat.toFloat() * scale)

                if (!member.isInsideGeofence) {
                    // Out-of-bounds Breach effect: Pulsing Crimson Alert Ripples
                    val rippleRadius = (16f + (alertRippleProgress * 26f)) * zoomScale.coerceIn(0.8f, 1.4f)
                    val rippleAlpha = (1.0f - alertRippleProgress).coerceIn(0f, 1f)
                    drawCircle(
                        color = NebulaColors.CriticalCrimson.copy(alpha = rippleAlpha * 0.7f),
                        radius = rippleRadius,
                        center = Offset(dotX, dotY),
                        style = Stroke(width = 2.5f)
                    )

                    drawCircle(
                        color = NebulaColors.CriticalCrimson,
                        radius = 9f * zoomScale.coerceIn(0.8f, 1.5f),
                        center = Offset(dotX, dotY)
                    )
                } else {
                    // In-bounds Safe Green Halo
                    drawCircle(
                        color = NebulaColors.SafeEmerald.copy(alpha = 0.3f),
                        radius = 12f * zoomScale.coerceIn(0.8f, 1.5f),
                        center = Offset(dotX, dotY)
                    )

                    drawCircle(
                        color = Color(member.avatarColorHex),
                        radius = 8f * zoomScale.coerceIn(0.8f, 1.5f),
                        center = Offset(dotX, dotY)
                    )
                }
            }
        }

        // Map UI Controls Overlay (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NebulaColors.DeepBackground.copy(alpha = 0.85f))
                    .border(1.dp, NebulaColors.CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f) }) {
                    Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NebulaColors.DeepBackground.copy(alpha = 0.85f))
                    .border(1.dp, NebulaColors.CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { zoomScale = (zoomScale * 0.80f).coerceAtLeast(0.6f) }) {
                    Text("−", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NebulaColors.DeepBackground.copy(alpha = 0.85f))
                    .border(1.dp, NebulaColors.CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { panOffset = Offset.Zero; zoomScale = 1.0f }) {
                    Text("🎯", fontSize = 13.sp)
                }
            }
        }

        // Map Legend / Geofence Scale Tag
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NebulaColors.DeepBackground.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NebulaColors.PrimaryIndigo)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${geofence.name} (${geofence.radiusMeters.toInt()}m)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextPrimary
            )
        }

        if (onMapCenterChange != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NebulaColors.PrimaryIndigoDark.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📍 Tap map to set center",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
