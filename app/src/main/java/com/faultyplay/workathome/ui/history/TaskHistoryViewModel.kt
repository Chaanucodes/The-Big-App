package com.faultyplay.workathome.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.datastore.UserPreferencesDataSource
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TaskHistoryViewModel @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<TaskHistoryUiState> = preferences.selectedHouseId.flatMapLatest { houseId ->
        if (houseId == null) {
            flowOf(emptyList())
        } else {
            flow {
                taskRepository.refresh(houseId)
                emitAll(taskRepository.observeTasks(houseId))
            }
        }
    }.combine(preferences.selectedHouseId) { tasks, houseId ->
        TaskHistoryUiState(
            houseId = houseId,
            inactiveTasks = tasks.filterNot { it.isActive }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskHistoryUiState())
}

data class TaskHistoryUiState(
    val houseId: String? = null,
    val inactiveTasks: List<Task> = emptyList()
)
