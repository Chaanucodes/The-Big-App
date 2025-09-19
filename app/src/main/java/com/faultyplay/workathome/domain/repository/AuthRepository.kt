package com.faultyplay.workathome.domain.repository

import com.faultyplay.workathome.domain.model.Member
import com.faultyplay.workathome.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserAccount?>
    suspend fun signIn(email: String, password: String): Result<UserAccount>
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String?
    ): Result<UserAccount>
    suspend fun signOut()
    suspend fun refreshUser(): UserAccount?
    suspend fun toMemberSnapshot(account: UserAccount): Member
}
