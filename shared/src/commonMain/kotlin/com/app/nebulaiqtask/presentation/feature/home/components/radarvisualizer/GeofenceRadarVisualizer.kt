package com.app.nebulaiqtask.presentation.feature.home.components.radarvisualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlin.math.sin

@Composable
fun GeofenceRadarVisualizer(
    geofence: GeofenceZone,
    members: List<GroupMember>,
    modifier: Modifier = Modifier,
    onMemberClicked: (String) -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()

    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NebulaColors.SurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(members) {
                    detectTapGestures { tapOffset ->
                        val minDim = minOf(size.width, size.height).toFloat()
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radiusPx = (minDim / 2f) * 0.70f

                        val centerLat = geofence.center.latitude
                        val centerLon = geofence.center.longitude
                        val metersToLat = 1.0 / 111000.0
                        val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))

                        for (member in members) {
                            val dLat = (member.currentLocation.latitude - centerLat) / metersToLat
                            val dLon = (member.currentLocation.longitude - centerLon) / metersToLon
                            val scale = radiusPx / geofence.radiusMeters.toFloat()
                            val dotX = centerX + (dLon.toFloat() * scale)
                            val dotY = centerY - (dLat.toFloat() * scale)

                            val dx = tapOffset.x - dotX
                            val dy = tapOffset.y - dotY
                            val distanceSq = dx * dx + dy * dy
                            if (distanceSq < 900f) {
                                onMemberClicked(member.id)
                                break
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadarRadius = size.minDimension / 2f * 0.70f

            // 1. Concentric guide rings (1/3, 2/3, 3/3 of geofence radius)
            for (i in 1..3) {
                val ringRadius = maxRadarRadius * (i / 3f)
                drawCircle(
                    color = NebulaColors.CardBorder.copy(alpha = 0.45f),
                    radius = ringRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                )
            }

            // 2. Crosshair grid lines
            drawLine(
                color = NebulaColors.CardBorder.copy(alpha = 0.35f),
                start = Offset(centerX, centerY - maxRadarRadius * 1.25f),
                end = Offset(centerX, centerY + maxRadarRadius * 1.25f),
                strokeWidth = 1f
            )
            drawLine(
                color = NebulaColors.CardBorder.copy(alpha = 0.35f),
                start = Offset(centerX - maxRadarRadius * 1.25f, centerY),
                end = Offset(centerX + maxRadarRadius * 1.25f, centerY),
                strokeWidth = 1f
            )

            // 3. Geofence Boundary (Solid Electric Indigo perimeter)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NebulaColors.PrimaryIndigo.copy(alpha = 0.08f),
                        NebulaColors.PrimaryIndigo.copy(alpha = 0.22f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = maxRadarRadius
                ),
                radius = maxRadarRadius,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                color = NebulaColors.PrimaryIndigo,
                radius = maxRadarRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3.5f)
            )

            // 4. Animated radar sweep line
            val sweepRad = (sweepAngle * (kotlin.math.PI / 180.0)).toFloat()
            val sweepEndX = centerX + cos(sweepRad) * maxRadarRadius
            val sweepEndY = centerY + sin(sweepRad) * maxRadarRadius
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(NebulaColors.AccentCyan.copy(alpha = 0.6f), Color.Transparent),
                    start = Offset(centerX, centerY),
                    end = Offset(sweepEndX, sweepEndY)
                ),
                start = Offset(centerX, centerY),
                end = Offset(sweepEndX, sweepEndY),
                strokeWidth = 2f
            )

            // 5. Center geofence hub
            drawCircle(
                color = NebulaColors.PrimaryIndigo,
                radius = 7f,
                center = Offset(centerX, centerY)
            )

            // 6. Member dots
            val centerLat = geofence.center.latitude
            val centerLon = geofence.center.longitude
            val metersToLat = 1.0 / 111000.0
            val metersToLon = 1.0 / (111000.0 * cos(centerLat * (kotlin.math.PI / 180.0)))
            val scale = maxRadarRadius / geofence.radiusMeters.toFloat()

            for (member in members) {
                val dLat = (member.currentLocation.latitude - centerLat) / metersToLat
                val dLon = (member.currentLocation.longitude - centerLon) / metersToLon
                val dotX = centerX + (dLon.toFloat() * scale)
                val dotY = centerY - (dLat.toFloat() * scale)

                if (!member.isInsideGeofence) {
                    // Breach effect: Expanding crimson alert ripples
                    val rippleRadius = 14f + (pulseProgress * 22f)
                    val rippleAlpha = (1.0f - pulseProgress).coerceIn(0f, 1f)
                    drawCircle(
                        color = NebulaColors.CriticalCrimson.copy(alpha = rippleAlpha * 0.7f),
                        radius = rippleRadius,
                        center = Offset(dotX, dotY),
                        style = Stroke(width = 2.5f)
                    )

                    // Core crimson dot
                    drawCircle(
                        color = NebulaColors.CriticalCrimson,
                        radius = 8.5f,
                        center = Offset(dotX, dotY)
                    )
                } else {
                    // Safe dot: Calm emerald glow
                    drawCircle(
                        color = NebulaColors.SafeEmerald.copy(alpha = 0.28f),
                        radius = 11f,
                        center = Offset(dotX, dotY)
                    )
                    drawCircle(
                        color = Color(member.avatarColorHex),
                        radius = 7f,
                        center = Offset(dotX, dotY)
                    )
                }
            }
        }

        // Overlay status badges
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NebulaColors.DeepBackground.copy(alpha = 0.75f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NebulaColors.AccentCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RADAR: ${geofence.radiusMeters.toInt()}m SAFE ZONE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.TextSecondary
            )
        }

        val breachCount = members.count { !it.isInsideGeofence }
        if (breachCount > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NebulaColors.CriticalCrimsonContainer.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ $breachCount OUTSIDE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
