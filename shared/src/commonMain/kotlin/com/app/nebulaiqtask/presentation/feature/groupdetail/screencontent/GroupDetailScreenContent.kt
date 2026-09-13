package com.app.nebulaiqtask.presentation.feature.groupdetail.screencontent

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
import com.app.nebulaiqtask.presentation.feature.groupdetail.components.geofencecard.GeofenceZoneCard
import com.app.nebulaiqtask.presentation.feature.groupdetail.components.membercard.MemberRosterCard
import com.app.nebulaiqtask.presentation.feature.groupdetail.intent.GroupDetailIntent
import com.app.nebulaiqtask.presentation.feature.groupdetail.state.GroupDetailState
import com.app.nebulaiqtask.presentation.feature.groupdetail.state.MemberFilter
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreenContent(
    state: GroupDetailState,
    onIntent: (GroupDetailIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebulaColors.DeepBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.group?.name ?: "Group Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = NebulaColors.TextPrimary
                        )
                        Text(
                            text = "${state.members.size} Tracked Members",
                            fontSize = 11.sp,
                            color = NebulaColors.TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(GroupDetailIntent.OnBackClicked) }) {
                        Text("←", fontSize = 20.sp, color = NebulaColors.TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(GroupDetailIntent.OnViewAlertsClicked) }) {
                        Text("🔔", fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NebulaColors.DeepBackground)
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NebulaColors.PrimaryIndigo)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                // 1. Geofence info card
                state.group?.let { group ->
                    item {
                        GeofenceZoneCard(geofence = group.geofence)
                    }
                }

                // 2. Filter tabs: All (10) | Safe (x) | Breached (y)
                item {
                    val insideCount = state.members.count { it.isInsideGeofence }
                    val outsideCount = state.members.count { !it.isInsideGeofence }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterTab(
                            title = "All (${state.members.size})",
                            isSelected = state.selectedFilter == MemberFilter.ALL,
                            onClick = { onIntent(GroupDetailIntent.OnFilterSelected(MemberFilter.ALL)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterTab(
                            title = "Inside ($insideCount)",
                            isSelected = state.selectedFilter == MemberFilter.INSIDE,
                            onClick = { onIntent(GroupDetailIntent.OnFilterSelected(MemberFilter.INSIDE)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterTab(
                            title = "Breach ($outsideCount)",
                            isSelected = state.selectedFilter == MemberFilter.OUTSIDE_BREACH,
                            onClick = { onIntent(GroupDetailIntent.OnFilterSelected(MemberFilter.OUTSIDE_BREACH)) },
                            isAlert = outsideCount > 0,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Filtered member list
                val filteredMembers = when (state.selectedFilter) {
                    MemberFilter.ALL -> state.members
                    MemberFilter.INSIDE -> state.members.filter { it.isInsideGeofence }
                    MemberFilter.OUTSIDE_BREACH -> state.members.filter { !it.isInsideGeofence }
                }

                items(filteredMembers, key = { it.id }) { member ->
                    MemberRosterCard(
                        member = member,
                        onMemberClicked = { onIntent(GroupDetailIntent.OnMemberClicked(member.id)) },
                        onTriggerBreach = { onIntent(GroupDetailIntent.TriggerBreach(member.id)) },
                        onReturnToSafety = { onIntent(GroupDetailIntent.ReturnToSafety(member.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false
) {
    val backgroundColor = when {
        isSelected && isAlert -> NebulaColors.CriticalCrimson
        isSelected -> NebulaColors.PrimaryIndigo
        isAlert -> Color(0xFFFEF2F2)
        else -> NebulaColors.SurfaceDark
    }
    val borderColor = when {
        isSelected && isAlert -> NebulaColors.CriticalCrimson
        isSelected -> NebulaColors.PrimaryIndigo
        isAlert -> Color(0xFFFCA5A5)
        else -> NebulaColors.CardBorder
    }
    val textColor = when {
        isSelected -> Color.White
        isAlert -> NebulaColors.CriticalCrimson
        else -> NebulaColors.TextSecondary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
