package com.app.nebulaiqtask.presentation.feature.home.components.breachbanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.BreachAlert
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun BreachAlertBanner(
    alert: BreachAlert?,
    onAcknowledge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = alert != null && !alert.isAcknowledged,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        if (alert != null) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = NebulaColors.CriticalCrimsonContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                            contentDescription = "Breach Alert",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Geofence Breach Detected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "${alert.memberName} is ${alert.distanceOutsideMeters.toInt()}m outside safe perimeter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NebulaColors.TextPrimary
                        )
                        Text(
                            text = "Notified ${alert.notifiedMembersCount} other members",
                            fontSize = 11.sp,
                            color = NebulaColors.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onAcknowledge(alert.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaColors.CriticalCrimson,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Dismiss", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
