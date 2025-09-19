package com.faultyplay.workathome.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskUrgency {
    ULTRA,
    HIGH,
    MEDIUM,
    LOW
}

@Serializable
enum class ProgressType {
    ONE_TIME,
    PERCENTAGE,
    MILESTONES,
    TIME_BASED
}

@Serializable
data class Milestone(
    val name: String,
    val isCompleted: Boolean = false
)

@Serializable
data class TaskProgress(
    val progressValue: Int = 0,
    val milestones: List<Milestone> = emptyList(),
    val trackedTimeMinutes: Int = 0,
    val targetTimeMinutes: Int = 0
)

@Serializable
data class TaskSession(
    val sessionId: String,
    val taskId: String,
    val houseId: String,
    val completedAt: Long,
    val timeTakenMillis: Long? = null,
    val expenses: Double? = null,
    val progressType: ProgressType = ProgressType.ONE_TIME,
    val progressSnapshot: TaskProgress = TaskProgress(),
    val completedBy: String? = null,
    val createdBy: String? = null,
    val description: String? = null
)

@Serializable
data class Task(
    val id: String,
    val globalId: String,
    val houseId: String,
    val taskName: String,
    val createdAt: Long,
    val deadlineAt: Long? = null,
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val description: String? = null,
    val urgency: TaskUrgency = TaskUrgency.MEDIUM,
    val isActive: Boolean = true,
    val inProgress: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringDeadlineAt: Long? = null,
    val createdBy: String,
    val assignedTo: String? = null,
    val completedBy: String? = null,
    val timeTakenMillis: Long? = null,
    val expenses: Double? = null,
    val progress: TaskProgress = TaskProgress(),
    val progressType: ProgressType = ProgressType.ONE_TIME,
    val pastSessions: List<TaskSession> = emptyList(),
    val lastSyncedAt: Long = createdAt,
    val lastModifiedAt: Long = createdAt,
    val isDraft: Boolean = false
) {
    val isOverdue: Boolean
        get() = deadlineAt?.let { System.currentTimeMillis() > it } ?: false

    val urgencyScore: Int
        get() = when (urgency) {
            TaskUrgency.ULTRA -> 4
            TaskUrgency.HIGH -> 3
            TaskUrgency.MEDIUM -> 2
            TaskUrgency.LOW -> 1
        }
}
