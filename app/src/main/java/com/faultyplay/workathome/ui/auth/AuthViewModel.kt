package com.faultyplay.workathome.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.domain.model.UserAccount
import com.faultyplay.workathome.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthFormState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignInMode: Boolean = true
)

data class AuthUiState(
    val user: UserAccount? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val form: AuthFormState = AuthFormState()
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val formState = MutableStateFlow(AuthFormState())
    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AuthUiState> = combine(
        authRepository.currentUser,
        formState,
        isLoading,
        errorMessage
    ) { user, form, loading, error ->
        AuthUiState(
            user = user,
            form = form,
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthUiState())

    fun onNameChanged(value: String) {
        formState.value = formState.value.copy(name = value)
    }

    fun onEmailChanged(value: String) {
        formState.value = formState.value.copy(email = value)
    }

    fun onPhoneChanged(value: String) {
        formState.value = formState.value.copy(phone = value)
    }

    fun onPasswordChanged(value: String) {
        formState.value = formState.value.copy(password = value)
    }

    fun onConfirmPasswordChanged(value: String) {
        formState.value = formState.value.copy(confirmPassword = value)
    }

    fun toggleMode() {
        val current = formState.value
        formState.value = current.copy(isSignInMode = !current.isSignInMode)
        errorMessage.value = null
    }

    fun submit() {
        val form = formState.value
        if (form.email.isBlank() || form.password.length < 6) {
            errorMessage.value = "Please enter a valid email and password"
            return
        }
        if (!form.isSignInMode && form.password != form.confirmPassword) {
            errorMessage.value = "Passwords do not match"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = if (form.isSignInMode) {
                authRepository.signIn(form.email.trim(), form.password)
            } else {
                authRepository.signUp(
                    name = form.name.ifBlank { form.email.substringBefore('@') },
                    email = form.email.trim(),
                    password = form.password,
                    phone = form.phone.takeIf { it.isNotBlank() }
                )
            }
            result.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Authentication failed"
            }
            isLoading.value = false
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
