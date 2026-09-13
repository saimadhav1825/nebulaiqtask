package com.app.nebulaiqtask.presentation.feature.alerts.screencontent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.app.nebulaiqtask.presentation.feature.alerts.components.alertitem.AlertIncidentCard
import com.app.nebulaiqtask.presentation.feature.alerts.components.notificationlog.DeliveredNotificationCard
import com.app.nebulaiqtask.presentation.feature.alerts.intent.AlertsIntent
import com.app.nebulaiqtask.presentation.feature.alerts.state.AlertsState
import com.app.nebulaiqtask.presentation.feature.alerts.state.AlertsTab
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreenContent(
    state: AlertsState,
    onIntent: (AlertsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebulaColors.DeepBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Incident Log & Alerts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NebulaColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(AlertsIntent.OnBackClicked) }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = NebulaColors.TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onIntent(AlertsIntent.ClearAllAlerts) }) {
                        Text("Clear All", fontSize = 12.sp, color = NebulaColors.TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NebulaColors.DeepBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tab Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    title = "Breach Incidents (${state.alerts.size})",
                    isSelected = state.selectedTab == AlertsTab.ACTIVE_BREACHES,
                    onClick = { onIntent(AlertsIntent.OnTabSelected(AlertsTab.ACTIVE_BREACHES)) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Delivered (${state.notifications.size})",
                    isSelected = state.selectedTab == AlertsTab.DELIVERED_NOTIFICATIONS,
                    onClick = { onIntent(AlertsIntent.OnTabSelected(AlertsTab.DELIVERED_NOTIFICATIONS)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Content
            when (state.selectedTab) {
                AlertsTab.ACTIVE_BREACHES -> {
                    if (state.alerts.isEmpty()) {
                        EmptyStateBox(message = "No geofence breach incidents recorded. All 10 members are safely inside the perimeter.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(state.alerts, key = { it.id }) { alert ->
                                AlertIncidentCard(
                                    alert = alert,
                                    onAcknowledge = { onIntent(AlertsIntent.AcknowledgeAlert(it)) }
                                )
                            }
                        }
                    }
                }
                AlertsTab.DELIVERED_NOTIFICATIONS -> {
                    if (state.notifications.isEmpty()) {
                        EmptyStateBox(message = "No notifications dispatched yet. When someone exits the geofence, alerts sent to other members appear here.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(state.notifications, key = { it.id }) { event ->
                                DeliveredNotificationCard(event = event)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NebulaColors.PrimaryIndigo else NebulaColors.CardElevated)
            .border(1.dp, if (isSelected) Color.Transparent else NebulaColors.CardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else NebulaColors.TextSecondary
        )
    }
}

@Composable
private fun EmptyStateBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NebulaColors.SafeEmerald,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "All Clear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NebulaColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = NebulaColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
