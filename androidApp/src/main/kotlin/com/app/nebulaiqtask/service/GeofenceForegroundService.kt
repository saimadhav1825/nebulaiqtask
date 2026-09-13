package com.app.nebulaiqtask.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.app.nebulaiqtask.MainActivity
import com.app.nebulaiqtask.data.datasource.LocalGroupDataSource
import com.app.nebulaiqtask.domain.model.MemberRole
import com.app.nebulaiqtask.domain.repository.MemberRepository
import com.app.nebulaiqtask.domain.repository.TrackingGroupRepository
import com.app.nebulaiqtask.domain.repository.UserRepository
import com.app.nebulaiqtask.domain.usecase.CheckGeofenceBreachUseCase
import com.app.nebulaiqtask.domain.usecase.GeofenceTransition
import com.app.nebulaiqtask.domain.usecase.SendBreachNotificationUseCase
import com.app.nebulaiqtask.presentation.platform.PlatformDeviceTelemetry
import com.app.nebulaiqtask.presentation.platform.PlatformLocationTracker
import com.app.nebulaiqtask.presentation.platform.PlatformNotificationDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceForegroundService : Service(), KoinComponent {

    companion object {
        const val NOTIFICATION_ID = 991
        const val CHANNEL_ID = "geofence_tracking_service"
    }

    private val locationTracker: PlatformLocationTracker by inject()
    private val localGroupDataSource: LocalGroupDataSource by inject()
    private val trackingGroupRepository: TrackingGroupRepository by inject()
    private val memberRepository: MemberRepository by inject()
    private val userRepository: UserRepository by inject()
    private val checkGeofenceBreachUseCase: CheckGeofenceBreachUseCase by inject()
    private val sendBreachNotificationUseCase: SendBreachNotificationUseCase by inject()
    private val platformDispatcher: PlatformNotificationDispatcher by inject()
    private val deviceTelemetry: PlatformDeviceTelemetry by inject()

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var trackingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildForegroundNotification("Initializing Geofence Monitoring..."))
        startBackgroundMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startBackgroundMonitoring() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            // Restore active tracking group from persistent storage if memory cache is empty
            val savedGroupId = userRepository.getActiveGroupId()
            if (savedGroupId != null && localGroupDataSource.groups.value.isEmpty()) {
                trackingGroupRepository.getTrackingGroup(savedGroupId)
            }

            localGroupDataSource.groups.collectLatest { groupsMap ->
                val activeGroup = groupsMap.values.firstOrNull { it.isTrackingActive }
                if (activeGroup != null) {
                    val groupDomain = trackingGroupRepository.getTrackingGroup(activeGroup.id)
                    if (groupDomain != null) {
                        updateNotification("🛡️ Monitoring ${activeGroup.members.size} members in ${activeGroup.geofence.name}")
                        monitorGroup(groupDomain.id)
                    }
                } else {
                    updateNotification("Awaiting active tracking group...")
                }
            }
        }
    }

    private fun CoroutineScope.monitorGroup(groupId: String) {
        // 1. Background GPS collection for local member
        launch {
            val myUserId = userRepository.currentUserProfile.value.userId
            locationTracker.startLocationUpdates().collectLatest { coord ->
                val currentGroup = trackingGroupRepository.getTrackingGroup(groupId) ?: return@collectLatest
                val localMember = currentGroup.members.find { it.id == myUserId || it.isLocalUser }
                if (localMember != null) {
                    val battery = deviceTelemetry.getBatteryPercentage()
                    val check = checkGeofenceBreachUseCase(
                        groupId = currentGroup.id,
                        member = localMember.copy(currentLocation = coord),
                        fence = currentGroup.geofence,
                        totalGroupMembersCount = currentGroup.members.size
                    )

                    memberRepository.updateMemberLocation(
                        groupId = currentGroup.id,
                        memberId = localMember.id,
                        location = coord,
                        battery = battery,
                        isInside = check.isInside,
                        distanceToFence = check.distanceOutsideMeters
                    )

                    // The member walking outside does NOT receive notification on their device
                    if (check.transition == GeofenceTransition.TRANSITION_ENTER) {
                        sendBreachNotificationUseCase.onMemberReturnedToSafety(
                            groupId = currentGroup.id,
                            memberId = localMember.id,
                            memberName = localMember.name
                        )
                    }
                }
            }
        }

        // 2. Background observation of other group members from Firebase
        launch {
            val myUserId = userRepository.currentUserProfile.value.userId
            trackingGroupRepository.getTrackingGroupFlow(groupId).collectLatest { group ->
                if (group != null && group.members.isNotEmpty()) {
                    val isCurrentUserOwner = group.members.any {
                        (it.id == myUserId || it.isLocalUser) && it.role == MemberRole.LEADER
                    }
                    var breachCount = 0
                    for (member in group.members) {
                        val result = checkGeofenceBreachUseCase(
                            groupId = groupId,
                            member = member,
                            fence = group.geofence,
                            totalGroupMembersCount = group.members.size
                        )

                        val memberAlert = result.generatedAlert
                        // ONLY the group owner receives breach notifications on their device
                        if (memberAlert != null && isCurrentUserOwner) {
                            sendBreachNotificationUseCase(
                                alert = memberAlert,
                                groupName = group.name,
                                recipientCount = group.members.size - 1,
                                isLocalUserOwner = true
                            )
                        } else if (result.transition == GeofenceTransition.TRANSITION_ENTER && isCurrentUserOwner) {
                            sendBreachNotificationUseCase.onMemberReturnedToSafety(
                                groupId = groupId,
                                memberId = member.id,
                                memberName = member.name
                            )
                        }
                        if (!result.isInside && member.role != MemberRole.LEADER) {
                            breachCount++
                        }
                    }

                    if (isCurrentUserOwner) {
                        if (breachCount > 0) {
                            updateNotification("🚨 $breachCount member(s) outside ${group.geofence.name}!")
                        } else {
                            updateNotification("🛡️ All ${group.members.size} members inside ${group.geofence.name}")
                        }
                    } else {
                        updateNotification("🛡️ Tracking active in ${group.geofence.name}")
                    }
                }
            }
        }
    }

    private fun buildForegroundNotification(statusText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nebula IQ Active Geofence")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildForegroundNotification(statusText))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        locationTracker.stopLocationUpdates()
    }
}
