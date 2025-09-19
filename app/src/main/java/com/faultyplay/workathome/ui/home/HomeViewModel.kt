package com.faultyplay.workathome.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.datastore.UserPreferencesDataSource
import com.faultyplay.workathome.domain.model.House
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.model.UserAccount
import com.faultyplay.workathome.domain.repository.AuthRepository
import com.faultyplay.workathome.domain.repository.HouseRepository
import com.faultyplay.workathome.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val houseRepository: HouseRepository,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val preferences: UserPreferencesDataSource
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val houseFlow = preferences.selectedHouseId.flatMapLatest { houseId ->
        if (houseId == null) {
            flowOf<House?>(null)
        } else {
            flow { emitAll(houseRepository.observeHouse(houseId)) }
        }
    }

    private val tasksFlow = preferences.selectedHouseId.flatMapLatest { houseId ->
        if (houseId == null) {
            flowOf(emptyList())
        } else {
            flow {
                isLoading.value = true
                taskRepository.refresh(houseId)
                emitAll(taskRepository.observeTasks(houseId))
                isLoading.value = false
            }
        }
    }.distinctUntilChanged()

    private val currentUserState = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<HomeUiState> = combine(
        houseFlow,
        tasksFlow,
        searchQuery,
        isLoading,
        errorMessage,
        currentUserState
    ) { values ->
        val house = values[0] as House?
        val tasks = values[1] as List<Task>
        val query = values[2] as String
        val loading = values[3] as Boolean
        val error = values[4] as String?
        val currentUser = values[5] as UserAccount?
        val activeTasks = tasks.filter { it.isActive }.filter { task ->
            query.isBlank() || task.taskName.contains(query, ignoreCase = true) ||
                (task.description?.contains(query, ignoreCase = true) ?: false)
        }
        val inactiveTasks = tasks.filterNot { it.isActive }
        HomeUiState(
            house = house,
            activeTasks = activeTasks,
            inactiveTasks = inactiveTasks,
            searchQuery = query,
            isLoading = loading,
            errorMessage = error,
            currentMemberName = currentUser?.name
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setSearchQuery(value: String) {
        searchQuery.value = value
    }

    fun refresh() {
        val houseId = uiState.value.house?.id ?: return
        viewModelScope.launch {
            isLoading.value = true
            runCatching { taskRepository.refresh(houseId) }
                .onFailure { throwable -> errorMessage.value = throwable.message }
            isLoading.value = false
        }
    }

    fun startTask(task: Task) {
        viewModelScope.launch {
            taskRepository.markInProgress(task.id, task.houseId, true)
        }
    }

    fun stopTask(task: Task) {
        viewModelScope.launch {
            taskRepository.markInProgress(task.id, task.houseId, false)
        }
    }

    fun completeTask(task: Task, timeTakenMillis: Long? = null, expenses: Double? = null) {
        viewModelScope.launch {
            taskRepository.completeTask(
                taskId = task.id,
                houseId = task.houseId,
                completedBy = currentUserState.value?.name ?: "Someone",
                timeTakenMillis = timeTakenMillis,
                expenses = expenses,
                nextRecurringDeadline = task.recurringDeadlineAt
            )
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}

data class HomeUiState(
    val house: House? = null,
    val activeTasks: List<Task> = emptyList(),
    val inactiveTasks: List<Task> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentMemberName: String? = null
)
