package com.faultyplay.workathome.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SignInState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    private val _signInIntent = MutableSharedFlow<Intent>()
    val signInIntent = _signInIntent.asSharedFlow()

    fun signIn() {
        _signInIntent.tryEmit(googleSignInClient.signInIntent)
    }

    fun onSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            _state.value = SignInState(error = "Google Sign-In failed.")
            return
        }
        // For now, we will just consider getting the account a success.
        // Firebase authentication will be added later.
        _state.value = SignInState(isSuccess = true)
    }

    fun resetState() {
        _state.value = SignInState()
    }
}
