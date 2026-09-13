package com.app.nebulaiqtask.presentation.feature.alerts.components.notificationlog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.domain.model.NotificationEvent
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@Composable
fun DeliveredNotificationCard(
    event: NotificationEvent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📲", fontSize = 18.sp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Delivered to ${event.recipientName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebulaColors.TextPrimary
                )
                Text(
                    text = event.message,
                    fontSize = 11.sp,
                    color = NebulaColors.TextSecondary
                )
            }

            Text(
                text = "✓ Pushed",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NebulaColors.SafeEmerald
            )
        }
    }
}
