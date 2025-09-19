package com.faultyplay.workathome.data.local.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.faultyplay.workathome.domain.model.Member
import com.faultyplay.workathome.domain.model.Milestone
import com.faultyplay.workathome.domain.model.ReminderSettings
import com.faultyplay.workathome.domain.model.TaskProgress
import com.faultyplay.workathome.domain.model.TaskSession
import com.faultyplay.workathome.domain.model.TaskSuggestion
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ProvidedTypeConverter
class SerializationConverters @Inject constructor(
    private val json: Json
) {
    @TypeConverter
    fun fromStringList(value: String?): List<String> =
        value?.let { json.decodeFromString(ListSerializer(String.serializer()), it) } ?: emptyList()

    @TypeConverter
    fun stringListToString(list: List<String>?): String =
        json.encodeToString(ListSerializer(String.serializer()), list ?: emptyList())

    @TypeConverter
    fun fromMembers(value: String?): List<Member> =
        value?.let { json.decodeFromString(ListSerializer(Member.serializer()), it) } ?: emptyList()

    @TypeConverter
    fun membersToString(list: List<Member>?): String =
        json.encodeToString(ListSerializer(Member.serializer()), list ?: emptyList())

    @TypeConverter
    fun fromReminderSettings(value: String?): ReminderSettings? =
        value?.let { json.decodeFromString(ReminderSettings.serializer(), it) }

    @TypeConverter
    fun reminderSettingsToString(value: ReminderSettings?): String? =
        value?.let { json.encodeToString(ReminderSettings.serializer(), it) }

    @TypeConverter
    fun fromTaskProgress(value: String?): TaskProgress? =
        value?.let { json.decodeFromString(TaskProgress.serializer(), it) }

    @TypeConverter
    fun taskProgressToString(value: TaskProgress?): String? =
        value?.let { json.encodeToString(TaskProgress.serializer(), it) }

    @TypeConverter
    fun fromTaskSessions(value: String?): List<TaskSession> =
        value?.let { json.decodeFromString(ListSerializer(TaskSession.serializer()), it) } ?: emptyList()

    @TypeConverter
    fun taskSessionsToString(value: List<TaskSession>?): String =
        json.encodeToString(ListSerializer(TaskSession.serializer()), value ?: emptyList())

    @TypeConverter
    fun fromMilestones(value: String?): List<Milestone> =
        value?.let { json.decodeFromString(ListSerializer(Milestone.serializer()), it) } ?: emptyList()

    @TypeConverter
    fun milestonesToString(value: List<Milestone>?): String =
        json.encodeToString(ListSerializer(Milestone.serializer()), value ?: emptyList())

    @TypeConverter
    fun fromTaskSuggestions(value: String?): List<TaskSuggestion> =
        value?.let { json.decodeFromString(ListSerializer(TaskSuggestion.serializer()), it) } ?: emptyList()

    @TypeConverter
    fun taskSuggestionsToString(value: List<TaskSuggestion>?): String =
        json.encodeToString(ListSerializer(TaskSuggestion.serializer()), value ?: emptyList())
}
