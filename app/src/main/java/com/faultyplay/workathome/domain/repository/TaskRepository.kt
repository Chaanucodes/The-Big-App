package com.faultyplay.workathome.domain.repository

import com.faultyplay.workathome.domain.model.ProgressType
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.model.TaskSession
import com.faultyplay.workathome.domain.model.TaskSuggestion
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(houseId: String): Flow<List<Task>>
    suspend fun refresh(houseId: String)
    suspend fun upsertTask(task: Task, overrideConflicts: Boolean = false)
    suspend fun markInProgress(taskId: String, houseId: String, inProgress: Boolean)
    suspend fun completeTask(
        taskId: String,
        houseId: String,
        completedBy: String,
        timeTakenMillis: Long?,
        expenses: Double?,
        nextRecurringDeadline: Long? = null
    )
    suspend fun deleteTask(taskId: String, houseId: String, clearHistory: Boolean)
    suspend fun saveDraft(task: Task)
    suspend fun loadDraft(houseId: String): Task?
    suspend fun clearDraft(houseId: String)
    fun observeSuggestions(query: String, progressType: ProgressType? = null): Flow<List<TaskSuggestion>>
    suspend fun recordSession(taskSession: TaskSession)
}
