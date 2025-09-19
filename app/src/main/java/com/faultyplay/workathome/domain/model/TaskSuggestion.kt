package com.faultyplay.workathome.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskSuggestion(
    val id: String,
    val name: String,
    val defaultProgressType: ProgressType = ProgressType.ONE_TIME,
    val defaultUrgency: TaskUrgency = TaskUrgency.MEDIUM,
    val defaultReminder: ReminderSettings = ReminderSettings(),
    val defaultIsRecurring: Boolean = false,
    val defaultRecurringIntervalDays: Int? = null,
    val defaultMilestones: List<String> = emptyList()
)
