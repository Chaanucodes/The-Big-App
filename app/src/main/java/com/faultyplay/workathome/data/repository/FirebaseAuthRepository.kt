package com.faultyplay.workathome.data.repository

import com.faultyplay.workathome.data.datastore.UserPreferencesDataSource
import com.faultyplay.workathome.domain.model.Member
import com.faultyplay.workathome.domain.model.UserAccount
import com.faultyplay.workathome.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val mutableUserFlow = MutableStateFlow(firebaseAuth.currentUser?.toDomain())
    override val currentUser: Flow<UserAccount?> = mutableUserFlow.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<UserAccount> = runCatching {
        withContext(dispatcher) {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("Missing user")
            val account = user.toDomain()
            mutableUserFlow.value = account
            userPreferencesDataSource.setLastUserId(account.id)
            account
        }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String?
    ): Result<UserAccount> = runCatching {
        withContext(dispatcher) {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("Missing user")
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdate).await()
            val account = user.toDomain(phoneOverride = phone)
            mutableUserFlow.value = account
            userPreferencesDataSource.setLastUserId(account.id)
            account
        }
    }

    override suspend fun signOut() {
        withContext(dispatcher) {
            firebaseAuth.signOut()
            mutableUserFlow.value = null
            userPreferencesDataSource.setLastUserId(null)
        }
    }

    override suspend fun refreshUser(): UserAccount? = withContext(dispatcher) {
        val user = firebaseAuth.currentUser ?: return@withContext null
        user.reload().await()
        val account = user.toDomain()
        mutableUserFlow.value = account
        account
    }

    override suspend fun toMemberSnapshot(account: UserAccount): Member = Member(
        id = account.id,
        houses = emptyList(),
        name = account.name,
        email = account.email,
        phone = account.phone,
        profileImageUrl = account.photoUrl
    )

    private fun com.google.firebase.auth.FirebaseUser.toDomain(phoneOverride: String? = null): UserAccount =
        UserAccount(
            id = uid,
            name = displayName ?: email?.substringBefore('@') ?: "Member",
            email = email ?: "",
            phone = phoneOverride ?: phoneNumber.orEmpty(),
            photoUrl = photoUrl?.toString()
        )
}
