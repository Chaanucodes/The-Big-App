package com.faultyplay.workathome.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class Urgency {
    LOW, MEDIUM, HIGH, ULTRA
}

data class Chore(
    val id: String = "",
    val taskName: String = "",
    val description: String = "",
    val urgency: Urgency = Urgency.MEDIUM,
    @ServerTimestamp
    val deadline: Date? = null,
    val isRecurring: Boolean = true,
    val recurrenceInterval: String = "Weekly", // e.g., "Weekly", "Monthly", or a custom value
    val createdBy: String = "", // User UID
    @ServerTimestamp
    val lastCompletedDate: Date? = null
)
