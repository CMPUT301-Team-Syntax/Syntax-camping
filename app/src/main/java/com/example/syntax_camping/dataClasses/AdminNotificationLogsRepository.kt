package com.example.syntax_camping.dataClasses

import com.google.firebase.functions.FirebaseFunctions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal enum class NotificationLogScope(val route: String) {
    CAMPSITE("campsite"),
    OWNER("owner")
}

internal data class NotificationLogEntry(
    val id: String,
    val campsiteId: String,
    val campsiteName: String,
    val title: String,
    val message: String,
    val occurredAt: Long
)

internal data class NotificationLogCursor(val id: String, val occurredAt: Long)

internal data class NotificationLogPage(
    val name: String,
    val rows: List<NotificationLogEntry>,
    val next: NotificationLogCursor? = null
)

internal class AdminNotificationLogsRepository {
    private val functions = FirebaseFunctions.getInstance("northamerica-northeast1")

    suspend fun load(
        scope: NotificationLogScope,
        id: String,
        cursor: NotificationLogCursor?
    ): Result<NotificationLogPage> = suspendCancellableCoroutine { continuation ->
        val arguments = mutableMapOf<String, Any>("scope" to scope.route, "id" to id)
        cursor?.let { arguments["cursor"] = mapOf("id" to it.id, "occurredAt" to it.occurredAt) }
        functions.getHttpsCallable("getAdminNotificationLogs").call(arguments)
            .addOnSuccessListener { result ->
                continuation.resume(runCatching { parsePage(result.data) })
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
    }

    private fun parsePage(value: Any?): NotificationLogPage {
        val page = value as? Map<*, *> ?: error("Invalid notification log page")
        val rows = (page["rows"] as? List<*> ?: error("Invalid notification log rows")).map { value ->
            val row = value as? Map<*, *> ?: error("Invalid notification log row")
            NotificationLogEntry(
                id = row["id"] as String,
                campsiteId = row["campsiteId"] as String,
                campsiteName = row["campsiteName"] as String,
                title = row["title"] as String,
                message = row["message"] as String,
                occurredAt = (row["occurredAt"] as Number).toLong()
            )
        }
        val next = (page["next"] as? Map<*, *>)?.let {
            NotificationLogCursor(it["id"] as String, (it["occurredAt"] as Number).toLong())
        }
        return NotificationLogPage(page["name"] as String, rows, next)
    }
}
