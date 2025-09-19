package com.faultyplay.workathome.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    val houses: List<String> = emptyList(),
    val name: String,
    val email: String,
    val phone: String,
    @SerialName("profileImageUrl")
    val profileImageUrl: String? = null
) {
    val initials: String
        get() {
            val parts = name.trim().split(" ")
            if (parts.isEmpty()) return "?"
            val builder = StringBuilder()
            builder.append(parts.first().firstOrNull()?.uppercaseChar() ?: '?')
            if (parts.size > 1) {
                builder.append(parts.last().firstOrNull()?.uppercaseChar() ?: '?')
            }
            return builder.toString().ifEmpty { "?" }
        }
}
