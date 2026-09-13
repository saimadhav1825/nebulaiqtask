package com.app.nebulaiqtask.data.datasource

import com.app.nebulaiqtask.data.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Duration.Companion.milliseconds

@Serializable
internal data class FirebaseGroupPayload(
    val id: String,
    val name: String,
    val geofence: GeofenceZoneDto,
    val members: Map<String, MemberDto> = emptyMap(),
    val activeAlertsCount: Int = 0,
    val isTrackingActive: Boolean = true,
    val createdAt: Long = 0L,
    val alerts: Map<String, BreachAlertDto> = emptyMap()
)

class FirebaseGroupDataSource(
    databaseUrl: String = "https://nebulaiqtask-default-rtdb.asia-southeast1.firebasedatabase.app"
) {
    private val databaseUrl: String = databaseUrl.trimEnd('/')
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private fun groupEndpoint(groupCode: String): String {
        val sanitized = groupCode.trim().uppercase()
        return "$databaseUrl/groups/$sanitized.json"
    }

    private fun memberEndpoint(groupCode: String, memberId: String): String {
        val sanitizedGroup = groupCode.trim().uppercase()
        val sanitizedMember = memberId.trim()
        return "$databaseUrl/groups/$sanitizedGroup/members/$sanitizedMember.json"
    }

    private fun alertsEndpoint(groupCode: String): String {
        val sanitizedGroup = groupCode.trim().uppercase()
        return "$databaseUrl/groups/$sanitizedGroup/alerts.json"
    }

    suspend fun saveGroup(group: GroupDto): Result<String> {
        return runCatching {
            val payload = FirebaseGroupPayload(
                id = group.id.uppercase(),
                name = group.name,
                geofence = group.geofence,
                members = group.members.associateBy { it.id },
                activeAlertsCount = group.activeAlertsCount,
                isTrackingActive = group.isTrackingActive,
                createdAt = group.createdAt
            )

            val response = client.put(groupEndpoint(group.id)) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            if (response.status.isSuccess()) {
                group.id.uppercase()
            } else {
                throw IllegalStateException("Failed to save group to Firebase: ${response.status}")
            }
        }
    }

    suspend fun getGroup(groupCode: String): Result<GroupDto?> {
        return runCatching {
            val response = client.get(groupEndpoint(groupCode))
            if (!response.status.isSuccess()) return@runCatching null

            val body = response.body<JsonObject?>() ?: return@runCatching null
            parseGroupFromJson(body)
        }
    }

    suspend fun joinGroup(groupCode: String, member: MemberDto): Result<GroupDto> {
        return runCatching {
            val sanitized = groupCode.trim().uppercase()
            val existing = getGroup(sanitized).getOrNull()
                ?: throw IllegalArgumentException("Group with code $sanitized does not exist.")

            // Put this member into the group
            client.put(memberEndpoint(sanitized, member.id)) {
                contentType(ContentType.Application.Json)
                setBody(member)
            }

            // Return refreshed group with new member added
            val updatedMembers = (existing.members.filterNot { it.id == member.id } + member)
            existing.copy(members = updatedMembers)
        }
    }

    suspend fun publishMemberLocation(
        groupCode: String,
        memberId: String,
        location: LocationDto,
        battery: Int,
        isInside: Boolean,
        distanceToFence: Double
    ): Result<Unit> {
        return runCatching {
            val updatePayload = mapOf(
                "currentLocation" to location,
                "batteryPercent" to battery,
                "isInsideGeofence" to isInside,
                "distanceToFenceMeters" to distanceToFence,
                "lastUpdatedMillis" to location.timestamp
            )

            client.patch(memberEndpoint(groupCode, memberId)) {
                contentType(ContentType.Application.Json)
                setBody(updatePayload)
            }
        }
    }

    suspend fun addMember(groupCode: String, member: MemberDto): Result<Unit> {
        return runCatching {
            client.put(memberEndpoint(groupCode, member.id)) {
                contentType(ContentType.Application.Json)
                setBody(member)
            }
        }
    }

    suspend fun removeMember(groupCode: String, memberId: String): Result<Unit> {
        return runCatching {
            client.delete(memberEndpoint(groupCode, memberId))
        }
    }

    suspend fun publishBreachAlert(groupCode: String, alert: BreachAlertDto): Result<Unit> {
        return runCatching {
            val sanitizedGroup = groupCode.trim().uppercase()
            val alertKey = alert.id.trim()
            val specificAlertUrl = "$databaseUrl/groups/$sanitizedGroup/alerts/$alertKey.json"
            client.put(specificAlertUrl) {
                contentType(ContentType.Application.Json)
                setBody(alert)
            }
        }
    }

    suspend fun getAlerts(groupCode: String): Result<List<BreachAlertDto>> {
        return runCatching {
            val response = client.get(alertsEndpoint(groupCode))
            if (!response.status.isSuccess()) return@runCatching emptyList()

            val body = response.body<JsonObject?>() ?: return@runCatching emptyList()
            val list = mutableListOf<BreachAlertDto>()
            for ((_, element) in body) {
                try {
                    list.add(json.decodeFromJsonElement<BreachAlertDto>(element))
                } catch (e: Exception) {
                    // Ignore malformed alert item
                }
            }
            list.sortedByDescending { it.timestamp }
        }
    }

    fun observeGroup(groupCode: String, pollIntervalMs: Long = 2000L): Flow<GroupDto?> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                val group = getGroup(groupCode).getOrNull()
                emit(group)
            } catch (e: Exception) {
                // Ignore transient network errors during continuous polling
            }
            delay(pollIntervalMs.milliseconds)
        }
    }

    private fun parseGroupFromJson(body: JsonObject): GroupDto {
        val id = body["id"]?.toString()?.replace("\"", "") ?: ""
        val name = body["name"]?.toString()?.replace("\"", "") ?: "Tracking Group"
        val geofenceElement = body["geofence"] ?: throw IllegalStateException("Missing geofence")
        val geofence = json.decodeFromJsonElement<GeofenceZoneDto>(geofenceElement)
        val isTrackingActive = body["isTrackingActive"]?.toString()?.toBooleanStrictOrNull() ?: true
        val alertsObj = body["alerts"] as? JsonObject
        val parsedAlertsCount = alertsObj?.size ?: 0
        val activeAlertsCount = body["activeAlertsCount"]?.toString()?.toIntOrNull() ?: parsedAlertsCount
        val createdAt = body["createdAt"]?.toString()?.toLongOrNull() ?: 0L

        // Members can be a JSON object map or an array
        val membersList = mutableListOf<MemberDto>()
        when (val membersElem = body["members"]) {
            is JsonObject -> {
                for ((_, memberElement) in membersElem) {
                    try {
                        val m = json.decodeFromJsonElement<MemberDto>(memberElement)
                        membersList.add(m)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            is JsonArray -> {
                for (memberElement in membersElem) {
                    if (memberElement is JsonNull) continue
                    try {
                        val m = json.decodeFromJsonElement<MemberDto>(memberElement)
                        membersList.add(m)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            else -> {}
        }

        return GroupDto(
            id = id,
            name = name,
            geofence = geofence,
            members = membersList,
            activeAlertsCount = activeAlertsCount,
            isTrackingActive = isTrackingActive,
            createdAt = createdAt
        )
    }
}
