package com.faultyplay.workathome.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.faultyplay.workathome.data.local.converter.SerializationConverters
import com.faultyplay.workathome.data.local.dao.HouseDao
import com.faultyplay.workathome.data.local.dao.TaskDao
import com.faultyplay.workathome.data.local.dao.TaskSuggestionDao
import com.faultyplay.workathome.data.local.entity.HouseEntity
import com.faultyplay.workathome.data.local.entity.TaskEntity
import com.faultyplay.workathome.data.local.entity.TaskSuggestionEntity

@Database(
    entities = [HouseEntity::class, TaskEntity::class, TaskSuggestionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(SerializationConverters::class)
abstract class WorkAtHomeDatabase : RoomDatabase() {
    abstract fun houseDao(): HouseDao
    abstract fun taskDao(): TaskDao
    abstract fun taskSuggestionDao(): TaskSuggestionDao
}
