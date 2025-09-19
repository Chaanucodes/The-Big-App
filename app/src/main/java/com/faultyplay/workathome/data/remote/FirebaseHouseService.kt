package com.faultyplay.workathome.data.remote

import com.faultyplay.workathome.domain.model.House
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.model.TaskSession
import com.faultyplay.workathome.domain.model.TaskSuggestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseHouseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val json: Json
) {
    private val housesCollection get() = firestore.collection("houses")
    private val suggestionsCollection get() = firestore.collection("task_suggestions")

    suspend fun fetchHousesForUser(userId: String): List<House> {
        val snapshot = housesCollection.whereArrayContains("memberIds", userId).get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.getString("payload")?.let { json.decodeFromString<House>(it) }
        }
    }

    suspend fun pushHouse(house: House) {
        val houseDocument = housesCollection.document(house.id)
        val payload = json.encodeToString(house)
        val memberIds = house.members.map { it.id }
        val data = mapOf(
            "payload" to payload,
            "memberIds" to memberIds,
            "allowedContacts" to house.allowedContacts,
            "inviteCode" to house.inviteCode
        )
        houseDocument.set(data).await()
    }

    suspend fun fetchHouseByInviteCode(inviteCode: String): House? {
        val snapshot = housesCollection.whereEqualTo("inviteCode", inviteCode).limit(1).get().await()
        val document = snapshot.documents.firstOrNull() ?: return null
        return document.getString("payload")?.let { json.decodeFromString<House>(it) }
    }

    suspend fun fetchTasks(houseId: String): List<Task> {
        val snapshot = housesCollection.document(houseId).collection("tasks").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.getString("payload")?.let { json.decodeFromString<Task>(it) }
        }
    }

    suspend fun pushTask(task: Task) {
        val payload = json.encodeToString(task)
        val data = mapOf(
            "payload" to payload,
            "taskName" to task.taskName,
            "houseId" to task.houseId,
            "updatedAt" to task.lastModifiedAt
        )
        housesCollection
            .document(task.houseId)
            .collection("tasks")
            .document(task.id)
            .set(data)
            .await()

        persistSuggestion(TaskSuggestion(
            id = task.globalId,
            name = task.taskName,
            defaultProgressType = task.progressType,
            defaultUrgency = task.urgency,
            defaultReminder = task.reminderSettings,
            defaultIsRecurring = task.isRecurring,
            defaultRecurringIntervalDays = task.recurringDeadlineAt?.let { deadline ->
                val delta = task.deadlineAt?.let { deadline - task.createdAt }
                if (delta != null && delta > 0) (delta / MILLIS_IN_DAY).toInt() else null
            },
            defaultMilestones = task.progress.milestones.map { it.name }
        ))
    }

    suspend fun deleteTask(houseId: String, taskId: String) {
        housesCollection.document(houseId).collection("tasks").document(taskId).delete().await()
    }

    suspend fun deleteHouse(houseId: String) {
        housesCollection.document(houseId).delete().await()
    }

    suspend fun recordPastSession(houseId: String, session: TaskSession) {
        val payload = json.encodeToString(session)
        housesCollection
            .document(houseId)
            .collection("task_sessions")
            .document(session.sessionId)
            .set(mapOf("payload" to payload))
            .await()
    }

    suspend fun fetchSuggestions(): List<TaskSuggestion> {
        val snapshot = suggestionsCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.getString("payload")?.let { json.decodeFromString<TaskSuggestion>(it) }
        }
    }

    suspend fun persistSuggestion(suggestion: TaskSuggestion) {
        val payload = json.encodeToString(suggestion)
        suggestionsCollection.document(suggestion.id).set(
            mapOf(
                "payload" to payload,
                "name" to suggestion.name,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    companion object {
        private const val MILLIS_IN_DAY = 86_400_000L
    }
}
