package com.faultyplay.workathome.data

data class House(
    val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList(), // List of User UIDs
    val invitedEmails: List<String> = emptyList()
)
