package com.app.nebulaiqtask.presentation.feature.memberdetail.screencontent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.app.nebulaiqtask.presentation.feature.memberdetail.components.breachaction.MemberBreachActionCard
import com.app.nebulaiqtask.presentation.feature.memberdetail.components.telemetrycard.MemberTelemetryCard
import com.app.nebulaiqtask.presentation.feature.memberdetail.intent.MemberDetailIntent
import com.app.nebulaiqtask.presentation.feature.memberdetail.state.MemberDetailState
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreenContent(
    state: MemberDetailState,
    onIntent: (MemberDetailIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebulaColors.DeepBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.member?.name ?: "Member Telemetry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NebulaColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(MemberDetailIntent.OnBackClicked) }) {
                        Text("←", fontSize = 20.sp, color = NebulaColors.TextPrimary)
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
            val member = state.member
            if (member != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
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
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(member.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NebulaColors.TextPrimary
                                )
                                Text(
                                    text = "Role: ${member.role.name.replace("_", " ")}",
                                    fontSize = 13.sp,
                                    color = NebulaColors.TextSecondary
                                )
                            }
                        }
                    }

                    // Telemetry Card
                    MemberTelemetryCard(member = member)

                    // Breach Action Card
                    if (!member.isLocalUser) {
                        MemberBreachActionCard(
                            member = member,
                            onTriggerBreach = { onIntent(MemberDetailIntent.TriggerBreach) },
                            onReturnToSafety = { onIntent(MemberDetailIntent.ReturnToSafety) },
                            onSendPing = { onIntent(MemberDetailIntent.SendPingAlert) }
                        )
                    }
                }
            }
        }
    }
}
