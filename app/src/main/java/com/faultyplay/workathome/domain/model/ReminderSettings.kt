package com.faultyplay.workathome.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReminderFrequency {
    ASAP,
    DAILY,
    WEEKLY,
    WEEKENDS,
    MONTHLY,
    CUSTOM
}

@Serializable
data class ReminderSettings(
    val frequency: ReminderFrequency = ReminderFrequency.WEEKLY,
    val timeOfDayMillis: Long? = null,
    val customIntervalDays: Int? = null,
    val notifyAllImmediately: Boolean = false,
    val recipients: List<String> = emptyList()
)
