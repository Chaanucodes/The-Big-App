package com.faultyplay.workathome.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val photoUrl: String? = null
)
