package com.faultyplay.workathome.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faultyplay.workathome.data.local.entity.TaskSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskSuggestionDao {
    @Query(
        "SELECT * FROM task_suggestions WHERE name LIKE '%' || :query || '%' ORDER BY usageCount DESC LIMIT 25"
    )
    fun observeSuggestions(query: String): Flow<List<TaskSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuggestions(entities: List<TaskSuggestionEntity>)

    @Query("UPDATE task_suggestions SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: String)

    @Query("DELETE FROM task_suggestions")
    suspend fun clear()
}
