package com.faultyplay.workathome.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.faultyplay.workathome.domain.model.ProgressType
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.model.TaskProgress
import com.faultyplay.workathome.domain.model.TaskSession
import com.faultyplay.workathome.domain.model.TaskUrgency
import com.faultyplay.workathome.domain.model.ReminderSettings

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["houseId", "taskName"], unique = false)]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val houseId: String,
    val globalId: String,
    val taskName: String,
    val createdAt: Long,
    val deadlineAt: Long? = null,
    val reminderSettings: ReminderSettings? = null,
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
    val progress: TaskProgress? = null,
    val progressType: ProgressType = ProgressType.ONE_TIME,
    val pastSessions: List<TaskSession> = emptyList(),
    val lastSyncedAt: Long = createdAt,
    val lastModifiedAt: Long = createdAt,
    val isDraft: Boolean = false,
    val draftReason: String? = null
) {
    fun toDomain(): Task = Task(
        id = id,
        globalId = globalId,
        houseId = houseId,
        taskName = taskName,
        createdAt = createdAt,
        deadlineAt = deadlineAt,
        reminderSettings = reminderSettings ?: ReminderSettings(),
        description = description,
        urgency = urgency,
        isActive = isActive,
        inProgress = inProgress,
        isRecurring = isRecurring,
        recurringDeadlineAt = recurringDeadlineAt,
        createdBy = createdBy,
        assignedTo = assignedTo,
        completedBy = completedBy,
        timeTakenMillis = timeTakenMillis,
        expenses = expenses,
        progress = progress ?: TaskProgress(),
        progressType = progressType,
        pastSessions = pastSessions,
        lastSyncedAt = lastSyncedAt,
        lastModifiedAt = lastModifiedAt,
        isDraft = isDraft
    )

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            houseId = task.houseId,
            globalId = task.globalId,
            taskName = task.taskName,
            createdAt = task.createdAt,
            deadlineAt = task.deadlineAt,
            reminderSettings = task.reminderSettings,
            description = task.description,
            urgency = task.urgency,
            isActive = task.isActive,
            inProgress = task.inProgress,
            isRecurring = task.isRecurring,
            recurringDeadlineAt = task.recurringDeadlineAt,
            createdBy = task.createdBy,
            assignedTo = task.assignedTo,
            completedBy = task.completedBy,
            timeTakenMillis = task.timeTakenMillis,
            expenses = task.expenses,
            progress = task.progress,
            progressType = task.progressType,
            pastSessions = task.pastSessions,
            lastSyncedAt = task.lastSyncedAt,
            lastModifiedAt = task.lastModifiedAt,
            isDraft = task.isDraft
        )
    }
}
