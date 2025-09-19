package com.faultyplay.workathome.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: String,
    val name: String,
    val inviteCode: String,
    val ownerId: String,
    val members: List<Member> = emptyList(),
    val allowedContacts: List<String> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val suggestions: List<TaskSuggestion> = emptyList(),
    val lastSelectedAt: Long = System.currentTimeMillis()
) {
    fun canMemberJoin(member: Member): Boolean {
        if (allowedContacts.isEmpty()) return true
        return allowedContacts.any { contact ->
            contact.equals(member.email, ignoreCase = true) ||
                contact.replace(" ", "") == member.phone.replace(" ", "")
        }
    }

    fun findTaskByName(taskName: String): Task? = tasks.firstOrNull {
        it.taskName.equals(taskName, ignoreCase = true)
    }
}
