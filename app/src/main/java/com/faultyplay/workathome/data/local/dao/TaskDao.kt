package com.faultyplay.workathome.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faultyplay.workathome.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE houseId = :houseId AND isDraft = 0")
    fun observeTasks(houseId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDraft = 0")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE houseId = :houseId")
    suspend fun getTasks(houseId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(entity: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(entities: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE houseId = :houseId")
    suspend fun deleteByHouse(houseId: String)

    @Query("DELETE FROM tasks WHERE houseId = :houseId AND id = :taskId")
    suspend fun deleteTask(houseId: String, taskId: String)

    @Query("SELECT * FROM tasks WHERE houseId = :houseId AND isDraft = 1 LIMIT 1")
    suspend fun getDraft(houseId: String): TaskEntity?

    @Query("UPDATE tasks SET isDraft = 0 WHERE houseId = :houseId AND isDraft = 1")
    suspend fun clearDrafts(houseId: String)

    @Query(
        "UPDATE tasks SET inProgress = :inProgress, lastModifiedAt = :lastModifiedAt WHERE houseId = :houseId AND id = :taskId"
    )
    suspend fun updateProgressState(
        houseId: String,
        taskId: String,
        inProgress: Boolean,
        lastModifiedAt: Long
    )

    @Query(
        "UPDATE tasks SET isActive = :isActive, completedBy = :completedBy, timeTakenMillis = :timeTakenMillis, expenses = :expenses, lastModifiedAt = :lastModifiedAt WHERE houseId = :houseId AND id = :taskId"
    )
    suspend fun updateCompletionState(
        houseId: String,
        taskId: String,
        isActive: Boolean,
        completedBy: String?,
        timeTakenMillis: Long?,
        expenses: Double?,
        lastModifiedAt: Long
    )
}
