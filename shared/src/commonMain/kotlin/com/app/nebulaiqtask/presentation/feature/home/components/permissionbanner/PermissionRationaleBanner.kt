package com.app.nebulaiqtask.presentation.feature.home.components.permissionbanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun PermissionRationaleBanner(
    hasLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val needsPermission = !hasLocationPermission || !hasNotificationPermission

    AnimatedVisibility(
        visible = needsPermission,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NebulaColors.WarningAmberContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Permissions Required For Live Geofencing",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = when {
                                !hasLocationPermission && !hasNotificationPermission ->
                                    "Location (GPS) is needed to track your position in the group, and Notifications are required to alert you when members leave the geofence."
                                !hasLocationPermission ->
                                    "Location (GPS) permission is needed to track your real-time coordinates against the geofence."
                                else ->
                                    "Notification permission is needed to receive high-priority alerts when group members breach the perimeter."
                            },
                            fontSize = 12.sp,
                            color = NebulaColors.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Grant Permissions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
