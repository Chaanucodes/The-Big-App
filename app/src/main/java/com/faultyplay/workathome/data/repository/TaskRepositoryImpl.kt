package com.faultyplay.workathome.data.repository

import com.faultyplay.workathome.data.local.dao.TaskDao
import com.faultyplay.workathome.data.local.dao.TaskSuggestionDao
import com.faultyplay.workathome.data.local.entity.TaskEntity
import com.faultyplay.workathome.data.local.entity.TaskSuggestionEntity
import com.faultyplay.workathome.data.remote.FirebaseHouseService
import com.faultyplay.workathome.di.AppModule
import com.faultyplay.workathome.domain.model.ProgressType
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.model.TaskSession
import com.faultyplay.workathome.domain.model.TaskSuggestion
import com.faultyplay.workathome.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskSuggestionDao: TaskSuggestionDao,
    private val remote: FirebaseHouseService,
    @param:AppModule.IoDispatcher private val dispatcher: CoroutineDispatcher
) : TaskRepository {

    override fun observeTasks(houseId: String): Flow<List<Task>> =
        taskDao.observeTasks(houseId).map { tasks ->
            tasks.map { it.toDomain() }
                .sortedWith(compareByDescending<Task> { it.urgencyScore }
                    .thenBy { it.deadlineAt ?: Long.MAX_VALUE }
                    .thenBy { it.taskName.lowercase() })
        }

    override suspend fun refresh(houseId: String) {
        withContext(dispatcher) {
            val remoteTasks = remote.fetchTasks(houseId)
            val localTasks = taskDao.getTasks(houseId).filter { !it.isDraft }
            val localMap = localTasks.associateBy { it.id }
            val merged = mutableListOf<TaskEntity>()

            remoteTasks.forEach { remoteTask ->
                val local = localMap[remoteTask.id]
                if (local == null || remoteTask.lastModifiedAt >= local.lastModifiedAt) {
                    merged += TaskEntity.fromDomain(remoteTask)
                } else {
                    remote.pushTask(local.toDomain())
                    merged += local
                }
            }

            localTasks.filter { task -> remoteTasks.none { it.id == task.id } }
                .forEach { orphan ->
                    remote.pushTask(orphan.toDomain())
                    merged += orphan
                }

            if (merged.isNotEmpty()) {
                taskDao.upsertTasks(merged)
            }

            val remoteSuggestions = remote.fetchSuggestions()
            if (remoteSuggestions.isNotEmpty()) {
                val suggestionEntities = remoteSuggestions.map { TaskSuggestionEntity.fromDomain(it) }
                taskSuggestionDao.upsertSuggestions(suggestionEntities)
            }
        }
    }

    override suspend fun upsertTask(task: Task, overrideConflicts: Boolean) {
        withContext(dispatcher) {
            val finalTask = normalizeTask(task)
            val current = taskDao.getTasks(task.houseId).firstOrNull { it.id == finalTask.id }
            val shouldOverride = current == null || overrideConflicts || finalTask.lastModifiedAt >= (current?.lastModifiedAt
                ?: 0L)
            val entity = TaskEntity.fromDomain(finalTask)
            taskDao.upsertTask(entity)
            if (shouldOverride) {
                remote.pushTask(finalTask)
            }
            taskSuggestionDao.upsertSuggestions(
                listOf(
                    TaskSuggestionEntity.fromDomain(
                        TaskSuggestion(
                            id = finalTask.globalId,
                            name = finalTask.taskName,
                            defaultProgressType = finalTask.progressType,
                            defaultUrgency = finalTask.urgency,
                            defaultReminder = finalTask.reminderSettings,
                            defaultIsRecurring = finalTask.isRecurring,
                            defaultRecurringIntervalDays = finalTask.recurringDeadlineAt?.let { delta ->
                                finalTask.deadlineAt?.let { deadline -> ((deadline - finalTask.createdAt) / MILLIS_IN_DAY).toInt() }
                            },
                            defaultMilestones = finalTask.progress.milestones.map { it.name }
                        )
                    )
                )
            )
        }
    }

    override suspend fun markInProgress(taskId: String, houseId: String, inProgress: Boolean) {
        withContext(dispatcher) {
            val timestamp = System.currentTimeMillis()
            taskDao.updateProgressState(houseId, taskId, inProgress, timestamp)
            val current = taskDao.getTasks(houseId).firstOrNull { it.id == taskId }?.toDomain()
            current?.let { remote.pushTask(it.copy(inProgress = inProgress, lastModifiedAt = timestamp)) }
        }
    }

    override suspend fun completeTask(
        taskId: String,
        houseId: String,
        completedBy: String,
        timeTakenMillis: Long?,
        expenses: Double?,
        nextRecurringDeadline: Long?
    ) {
        withContext(dispatcher) {
            val timestamp = System.currentTimeMillis()
            taskDao.updateCompletionState(houseId, taskId, false, completedBy, timeTakenMillis, expenses, timestamp)
            val task = taskDao.getTasks(houseId).firstOrNull { it.id == taskId }?.toDomain() ?: return@withContext
            val session = TaskSession(
                sessionId = UUID.randomUUID().toString(),
                taskId = taskId,
                houseId = houseId,
                completedAt = timestamp,
                timeTakenMillis = timeTakenMillis,
                expenses = expenses,
                progressType = task.progressType,
                progressSnapshot = task.progress,
                completedBy = completedBy,
                createdBy = task.createdBy,
                description = task.description
            )
            val updatedTask = task.copy(
                isActive = false,
                inProgress = false,
                completedBy = completedBy,
                timeTakenMillis = timeTakenMillis,
                expenses = expenses,
                pastSessions = task.pastSessions + session,
                recurringDeadlineAt = nextRecurringDeadline,
                lastModifiedAt = timestamp
            )
            taskDao.upsertTask(TaskEntity.fromDomain(updatedTask))
            remote.pushTask(updatedTask)
            remote.recordPastSession(houseId, session)
            taskSuggestionDao.incrementUsage(updatedTask.globalId)
        }
    }

    override suspend fun deleteTask(taskId: String, houseId: String, clearHistory: Boolean) {
        withContext(dispatcher) {
            taskDao.deleteTask(houseId, taskId)
            remote.deleteTask(houseId, taskId)
            if (clearHistory) {
                remote.recordPastSession(
                    houseId,
                    TaskSession(
                        sessionId = UUID.randomUUID().toString(),
                        taskId = taskId,
                        houseId = houseId,
                        completedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override suspend fun saveDraft(task: Task) {
        withContext(dispatcher) {
            val draft = task.copy(isDraft = true)
            taskDao.upsertTask(TaskEntity.fromDomain(draft))
        }
    }

    override suspend fun loadDraft(houseId: String): Task? = withContext(dispatcher) {
        taskDao.getDraft(houseId)?.toDomain()
    }

    override suspend fun clearDraft(houseId: String) {
        withContext(dispatcher) { taskDao.clearDrafts(houseId) }
    }

    override fun observeSuggestions(
        query: String,
        progressType: ProgressType?
    ): Flow<List<TaskSuggestion>> = taskSuggestionDao.observeSuggestions(query).map { suggestions ->
        suggestions.map { it.toDomain() }
            .filter { suggestion -> progressType == null || suggestion.defaultProgressType == progressType }
    }

    override suspend fun recordSession(taskSession: TaskSession) {
        withContext(dispatcher) {
            remote.recordPastSession(taskSession.houseId, taskSession)
        }
    }

    private fun normalizeTask(task: Task): Task {
        val name = task.taskName.trim()
        val taskId = if (task.id.isNotEmpty()) task.id else UUID.randomUUID().toString()
        val globalId = task.globalId.ifEmpty { name.lowercase().replace(" ", "-") }
        val progress = task.progress.copy(
            progressValue = task.progress.progressValue.coerceIn(0, 100)
        )
        val reminder = if (task.urgencyScore == 4) {
            task.reminderSettings.copy(notifyAllImmediately = true)
        } else {
            task.reminderSettings
        }
        val recurringDeadline = if (task.isRecurring) {
            task.recurringDeadlineAt ?: task.deadlineAt?.let { deadline ->
                val duration = deadline - task.createdAt
                task.deadlineAt.plus(duration)
            }
        } else {
            null
        }
        return task.copy(
            id = taskId,
            globalId = globalId,
            taskName = name,
            progress = progress,
            reminderSettings = reminder,
            recurringDeadlineAt = recurringDeadline,
            lastModifiedAt = System.currentTimeMillis()
        )
    }

    companion object {
        private const val MILLIS_IN_DAY = 86_400_000L
    }
}
