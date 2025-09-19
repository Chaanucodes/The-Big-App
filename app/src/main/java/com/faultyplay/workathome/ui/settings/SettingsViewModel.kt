package com.faultyplay.workathome.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.datastore.ThemePreference
import com.faultyplay.workathome.data.datastore.UserPreferencesDataSource
import com.faultyplay.workathome.domain.model.House
import com.faultyplay.workathome.domain.model.UserAccount
import com.faultyplay.workathome.domain.repository.AuthRepository
import com.faultyplay.workathome.domain.repository.HouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val authRepository: AuthRepository,
    private val houseRepository: HouseRepository
) : ViewModel() {

    private val isSigningOut = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val userFlow = authRepository.currentUser
    private val housesFlow = userFlow.flatMapLatest { account ->
        if (account == null) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            houseRepository.observeHouses(account.id)
        }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.notificationsEnabled,
        preferences.themePreference,
        userFlow,
        housesFlow,
        isSigningOut,
        errorMessage
    ) { notifications, theme, user, houses, signingOut, error ->
        SettingsUiState(
            notificationsEnabled = notifications,
            themePreference = theme,
            account = user,
            houses = houses,
            isSigningOut = signingOut,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
        }
    }

    fun setThemePreference(themePreference: ThemePreference) {
        viewModelScope.launch {
            preferences.setThemePreference(themePreference)
        }
    }

    fun selectHouse(houseId: String) {
        viewModelScope.launch {
            preferences.setSelectedHouse(houseId)
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            isSigningOut.value = true
            runCatching { authRepository.signOut() }
                .onSuccess { onSignedOut() }
                .onFailure { throwable ->
                    errorMessage.value = throwable.message ?: "Unable to sign out"
                }
            isSigningOut.value = false
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val account: UserAccount? = null,
    val houses: List<House> = emptyList(),
    val isSigningOut: Boolean = false,
    val errorMessage: String? = null
)
