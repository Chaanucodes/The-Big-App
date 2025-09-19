package com.faultyplay.workathome.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.faultyplay.workathome.domain.model.ProgressType
import com.faultyplay.workathome.domain.model.ReminderSettings
import com.faultyplay.workathome.domain.model.TaskSuggestion
import com.faultyplay.workathome.domain.model.TaskUrgency

@Entity(tableName = "task_suggestions")
data class TaskSuggestionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val defaultProgressType: ProgressType = ProgressType.ONE_TIME,
    val defaultUrgency: TaskUrgency = TaskUrgency.MEDIUM,
    val defaultReminder: ReminderSettings? = null,
    val defaultIsRecurring: Boolean = false,
    val defaultRecurringIntervalDays: Int? = null,
    val defaultMilestones: List<String> = emptyList(),
    val usageCount: Int = 0
) {
    fun toDomain(): TaskSuggestion = TaskSuggestion(
        id = id,
        name = name,
        defaultProgressType = defaultProgressType,
        defaultUrgency = defaultUrgency,
        defaultReminder = defaultReminder ?: ReminderSettings(),
        defaultIsRecurring = defaultIsRecurring,
        defaultRecurringIntervalDays = defaultRecurringIntervalDays,
        defaultMilestones = defaultMilestones
    )

    companion object {
        fun fromDomain(suggestion: TaskSuggestion): TaskSuggestionEntity = TaskSuggestionEntity(
            id = suggestion.id,
            name = suggestion.name,
            defaultProgressType = suggestion.defaultProgressType,
            defaultUrgency = suggestion.defaultUrgency,
            defaultReminder = suggestion.defaultReminder,
            defaultIsRecurring = suggestion.defaultIsRecurring,
            defaultRecurringIntervalDays = suggestion.defaultRecurringIntervalDays,
            defaultMilestones = suggestion.defaultMilestones
        )
    }
}
