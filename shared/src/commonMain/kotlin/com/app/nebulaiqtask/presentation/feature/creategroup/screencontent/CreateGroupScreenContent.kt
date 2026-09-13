package com.app.nebulaiqtask.presentation.feature.creategroup.screencontent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.nebulaiqtask.presentation.feature.creategroup.components.presetpicker.GeofencePresetPicker
import com.app.nebulaiqtask.presentation.feature.creategroup.components.radiusslider.GeofenceRadiusSlider
import com.app.nebulaiqtask.presentation.feature.creategroup.intent.CreateGroupIntent
import com.app.nebulaiqtask.presentation.feature.creategroup.state.CreateGroupState
import com.app.nebulaiqtask.presentation.theme.NebulaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreenContent(
    state: CreateGroupState,
    onIntent: (CreateGroupIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NebulaColors.DeepBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Tracking Group",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NebulaColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(CreateGroupIntent.OnBackClicked) }) {
                        Text("←", fontSize = 20.sp, color = NebulaColors.TextPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Group Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. GROUP DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebulaColors.TextSecondary
                    )

                    OutlinedTextField(
                        value = state.groupName,
                        onValueChange = { onIntent(CreateGroupIntent.OnGroupNameChanged(it)) },
                        label = { Text("Group Name") },
                        placeholder = { Text("e.g. Field Operations Alpha") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaColors.PrimaryIndigo,
                            unfocusedBorderColor = NebulaColors.CardBorder,
                            focusedTextColor = NebulaColors.TextPrimary,
                            unfocusedTextColor = NebulaColors.TextPrimary
                        )
                    )

                    Text(
                        text = "Creates a group pre-populated with 10 members and instant broadcast notifications.",
                        fontSize = 11.sp,
                        color = NebulaColors.TextSecondary
                    )
                }
            }

            // Geofence Configuration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NebulaColors.CardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NebulaColors.SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "2. GEOFENCE CONFIGURATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebulaColors.TextSecondary
                    )

                    OutlinedTextField(
                        value = state.geofenceName,
                        onValueChange = { onIntent(CreateGroupIntent.OnGeofenceNameChanged(it)) },
                        label = { Text("Geofence Perimeter Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NebulaColors.PrimaryIndigo,
                            unfocusedBorderColor = NebulaColors.CardBorder,
                            focusedTextColor = NebulaColors.TextPrimary,
                            unfocusedTextColor = NebulaColors.TextPrimary
                        )
                    )

                    GeofencePresetPicker(
                        selectedIndex = state.selectedPresetIndex,
                        onSelectPreset = { onIntent(CreateGroupIntent.OnPresetSelected(it)) }
                    )

                    GeofenceRadiusSlider(
                        radiusMeters = state.radiusMeters,
                        onRadiusChanged = { onIntent(CreateGroupIntent.OnRadiusChanged(it)) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Notify All Members on Exit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NebulaColors.TextPrimary
                            )
                            Text(
                                "Heads-up alert fired to other 9 users immediately",
                                fontSize = 11.sp,
                                color = NebulaColors.TextSecondary
                            )
                        }

                        Switch(
                            checked = state.alertOnExit,
                            onCheckedChange = { onIntent(CreateGroupIntent.OnAlertOnExitToggled(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NebulaColors.PrimaryIndigo
                            )
                        )
                    }
                }
            }

            // Create Group Submit Button
            Button(
                onClick = { onIntent(CreateGroupIntent.OnCreateClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isSubmitting && state.groupName.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NebulaColors.PrimaryIndigo)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Create Group & Activate Geofence",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
