package com.faultyplay.workathome.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.datastore.UserPreferencesDataSource
import com.faultyplay.workathome.domain.model.House
import com.faultyplay.workathome.domain.model.Member
import com.faultyplay.workathome.domain.repository.AuthRepository
import com.faultyplay.workathome.domain.repository.HouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HouseListUiState(
    val houses: List<House> = emptyList(),
    val activeHouseId: String? = null,
    val inviteCode: String = "",
    val newHouseName: String = "",
    val allowedContacts: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentMember: Member? = null
)

@HiltViewModel
class HouseListViewModel @Inject constructor(
    private val houseRepository: HouseRepository,
    private val authRepository: AuthRepository,
    private val preferencesDataSource: UserPreferencesDataSource
) : ViewModel() {

    private val inviteCode = MutableStateFlow("")
    private val newHouseName = MutableStateFlow("")
    private val allowedContacts = MutableStateFlow("")
    private val loading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val currentMemberFlow = authRepository.currentUser.flatMapLatest { account ->
        if (account == null) {
            flowOf<Member?>(null)
        } else {
            flow {
                emit(authRepository.toMemberSnapshot(account))
            }
        }
    }

    private val housesFlow = authRepository.currentUser.flatMapLatest { account ->
        if (account == null) {
            flowOf(emptyList())
        } else {
            flow {
                houseRepository.refresh(account.id)
                emitAll(houseRepository.observeHouses(account.id))
            }
        }
    }

    val uiState: StateFlow<HouseListUiState> = combine(
        housesFlow,
        preferencesDataSource.selectedHouseId,
        inviteCode,
        newHouseName,
        allowedContacts,
        loading,
        errorMessage,
        currentMemberFlow
    ) { houses, selectedHouse, invite, name, allowed, isLoading, error, member ->
        HouseListUiState(
            houses = houses,
            activeHouseId = selectedHouse,
            inviteCode = invite,
            newHouseName = name,
            allowedContacts = allowed,
            isLoading = isLoading,
            errorMessage = error,
            currentMember = member
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HouseListUiState())

    fun onInviteCodeChanged(value: String) {
        inviteCode.value = value.uppercase()
    }

    fun onNewHouseNameChanged(value: String) {
        newHouseName.value = value
    }

    fun onAllowedContactsChanged(value: String) {
        allowedContacts.value = value
    }

    fun selectHouse(houseId: String) {
        viewModelScope.launch {
            preferencesDataSource.setSelectedHouse(houseId)
        }
    }

    fun joinHouse() {
        val member = uiState.value.currentMember ?: return
        val code = uiState.value.inviteCode.trim()
        if (code.length < 4) {
            errorMessage.value = "Enter a valid invite code"
            return
        }
        viewModelScope.launch {
            loading.value = true
            val result = houseRepository.joinHouse(member, code)
            result.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Unable to join house"
            }
            result.onSuccess { house ->
                preferencesDataSource.setSelectedHouse(house.id)
                inviteCode.value = ""
            }
            loading.value = false
        }
    }

    fun createHouse() {
        val member = uiState.value.currentMember ?: return
        val name = uiState.value.newHouseName.trim()
        if (name.isEmpty()) {
            errorMessage.value = "House name cannot be empty"
            return
        }
        val allowedContactsList = uiState.value.allowedContacts
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        viewModelScope.launch {
            loading.value = true
            runCatching {
                houseRepository.createHouse(member, name, allowedContactsList)
            }.onSuccess { house ->
                preferencesDataSource.setSelectedHouse(house.id)
                newHouseName.value = ""
                allowedContacts.value = ""
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Unable to create house"
            }
            loading.value = false
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}
