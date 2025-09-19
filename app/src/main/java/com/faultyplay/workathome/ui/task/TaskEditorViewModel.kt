package com.faultyplay.workathome.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.datastore.UserPreferencesDataSource
import com.faultyplay.workathome.domain.model.Member
import com.faultyplay.workathome.domain.model.ProgressType
import com.faultyplay.workathome.domain.model.ReminderFrequency
import com.faultyplay.workathome.domain.model.ReminderSettings
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.model.TaskProgress
import com.faultyplay.workathome.domain.model.TaskSuggestion
import com.faultyplay.workathome.domain.model.TaskUrgency
import com.faultyplay.workathome.domain.repository.AuthRepository
import com.faultyplay.workathome.domain.repository.HouseRepository
import com.faultyplay.workathome.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

private const val MILLIS_IN_DAY = 86_400_000L

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val houseRepository: HouseRepository,
    private val authRepository: AuthRepository,
    private val preferences: UserPreferencesDataSource
) : ViewModel() {

    private val houseIdState = MutableStateFlow(savedStateHandle.get<String>("houseId").orEmpty())
    private val taskId: String? = savedStateHandle.get<String>("taskId")

    private val draftState = MutableStateFlow(TaskDraft())
    private val isSaving = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val membersFlow = houseIdState.flatMapLatest { id ->
        if (id.isBlank()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            houseRepository.observeHouse(id).map { house -> house?.members ?: emptyList() }
        }
    }

    private val suggestionsFlow = draftState.flatMapLatest { draft ->
        if (draft.name.length < 3) {
            taskRepository.observeSuggestions("", draft.progressType)
        } else {
            taskRepository.observeSuggestions(draft.name, draft.progressType)
        }
    }

    val uiState: StateFlow<TaskEditorUiState> = combine(
        draftState,
        membersFlow,
        suggestionsFlow,
        isSaving,
        errorMessage
    ) { draft, members, suggestions, saving, error ->
        TaskEditorUiState(
            draft = draft,
            members = members,
            suggestions = suggestions,
            isSaving = saving,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskEditorUiState())

    init {
        viewModelScope.launch {
            if (houseIdState.value.isBlank()) {
                val preferred = preferences.selectedHouseId.first().orEmpty()
                houseIdState.value = preferred
            }
            val currentUser = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null).value
            val currentHouseId = houseIdState.value
            if (currentHouseId.isBlank()) return@launch
            val existingDraft = if (taskId == null) taskRepository.loadDraft(currentHouseId) else null
            when {
                taskId != null -> {
                    val task = taskRepository.observeTasks(currentHouseId).first().firstOrNull { it.id == taskId }
                    if (task != null) {
                        draftState.value = TaskDraft.fromTask(task)
                    }
                }
                existingDraft != null -> {
                    draftState.value = TaskDraft.fromTask(existingDraft)
                }
                currentUser != null -> {
                    draftState.value = draftState.value.copy(createdBy = currentUser.name)
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        draftState.value = draftState.value.copy(name = value)
    }

    fun onDescriptionChanged(value: String) {
        draftState.value = draftState.value.copy(description = value)
    }

    fun onUrgencyChanged(value: TaskUrgency) {
        draftState.value = draftState.value.copy(urgency = value)
    }

    fun onProgressTypeChanged(value: ProgressType) {
        draftState.value = draftState.value.copy(progressType = value)
    }

    fun onDeadlineDaysChanged(value: String) {
        draftState.value = draftState.value.copy(deadlineDays = value)
    }

    fun onRecurringToggle(value: Boolean) {
        draftState.value = draftState.value.copy(isRecurring = value)
    }

    fun onRecurrenceIntervalChanged(value: String) {
        draftState.value = draftState.value.copy(recurrenceIntervalDays = value.toIntOrNull())
    }

    fun onReminderFrequencyChanged(value: ReminderFrequency) {
        draftState.value = draftState.value.copy(reminderFrequency = value)
    }

    fun onReminderTimeChanged(value: String) {
        draftState.value = draftState.value.copy(reminderTime = value)
    }

    fun onRecipientToggled(memberId: String) {
        val draft = draftState.value
        draftState.value = if (memberId in draft.recipients) {
            draft.copy(recipients = draft.recipients - memberId)
        } else {
            draft.copy(recipients = draft.recipients + memberId)
        }
    }

    fun onAssignedMemberChanged(memberId: String?) {
        draftState.value = draftState.value.copy(assignedTo = memberId)
    }

    fun onMilestonesChanged(value: String) {
        draftState.value = draftState.value.copy(milestoneText = value)
    }

    fun onTargetHoursChanged(value: String) {
        draftState.value = draftState.value.copy(targetTimeHours = value)
    }

    fun applySuggestion(suggestion: TaskSuggestion) {
        draftState.value = draftState.value.copy(
            name = suggestion.name,
            progressType = suggestion.defaultProgressType,
            urgency = suggestion.defaultUrgency,
            isRecurring = suggestion.defaultIsRecurring,
            recurrenceIntervalDays = suggestion.defaultRecurringIntervalDays,
            milestoneText = suggestion.defaultMilestones.joinToString(separator = "\n")
        )
    }

    fun saveTask(onSaved: (Task) -> Unit) {
        val draft = draftState.value
        if (draft.name.isBlank()) {
            errorMessage.value = "Task name cannot be empty"
            return
        }
        viewModelScope.launch {
            isSaving.value = true
            val account = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null).value
            val houseId = houseIdState.value
            if (houseId.isBlank()) {
                errorMessage.value = "No house selected"
                isSaving.value = false
                return@launch
            }
            val task = draft.toTask(
                houseId = houseId,
                createdBy = account?.name ?: ""
            )
            runCatching { taskRepository.upsertTask(task) }
                .onSuccess {
                    taskRepository.clearDraft(houseId)
                    onSaved(task)
                }
                .onFailure { throwable ->
                    errorMessage.value = throwable.message ?: "Unable to save task"
                }
            isSaving.value = false
        }
    }

    fun saveDraft() {
        val houseId = houseIdState.value
        if (houseId.isBlank()) return
        val task = draftState.value.toTask(
            houseId = houseId,
            createdBy = draftState.value.createdBy,
            markAsDraft = true
        )
        viewModelScope.launch {
            taskRepository.saveDraft(task)
        }
    }

    fun discardDraft() {
        viewModelScope.launch {
            draftState.value = TaskDraft()
            val houseId = houseIdState.value
            if (houseId.isNotBlank()) {
                taskRepository.clearDraft(houseId)
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}

data class TaskEditorUiState(
    val draft: TaskDraft = TaskDraft(),
    val members: List<Member> = emptyList(),
    val suggestions: List<TaskSuggestion> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class TaskDraft(
    val taskId: String? = null,
    val globalId: String? = null,
    val name: String = "",
    val description: String = "",
    val urgency: TaskUrgency = TaskUrgency.MEDIUM,
    val progressType: ProgressType = ProgressType.ONE_TIME,
    val assignedTo: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceIntervalDays: Int? = null,
    val deadlineDays: String = "30",
    val reminderFrequency: ReminderFrequency = ReminderFrequency.WEEKLY,
    val reminderTime: String = "12:00",
    val recipients: Set<String> = emptySet(),
    val milestoneText: String = "",
    val targetTimeHours: String = "4",
    val createdBy: String = ""
) {
    companion object {
        fun fromTask(task: Task): TaskDraft = TaskDraft(
            taskId = task.id,
            globalId = task.globalId,
            name = task.taskName,
            description = task.description.orEmpty(),
            urgency = task.urgency,
            progressType = task.progressType,
            assignedTo = task.assignedTo,
            isRecurring = task.isRecurring,
            recurrenceIntervalDays = task.recurringDeadlineAt?.let { ((it - task.createdAt) / MILLIS_IN_DAY).toInt() },
            deadlineDays = task.deadlineAt?.let { ((it - task.createdAt) / MILLIS_IN_DAY).coerceAtLeast(1) }?.toString() ?: "30",
            reminderFrequency = task.reminderSettings.frequency,
            reminderTime = "12:00",
            recipients = task.reminderSettings.recipients.toSet(),
            milestoneText = task.progress.milestones.joinToString("\n") { it.name },
            targetTimeHours = (task.progress.targetTimeMinutes / 60).coerceAtLeast(1).toString(),
            createdBy = task.createdBy
        )
    }
}

private fun TaskDraft.toTask(
    houseId: String,
    createdBy: String,
    markAsDraft: Boolean = false
): Task {
    val now = System.currentTimeMillis()
    val deadlineMillis = deadlineDays.toIntOrNull()?.takeIf { it > 0 }?.let { days -> now + days * MILLIS_IN_DAY }
    val milestones = milestoneText.lines().mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty()) null else com.faultyplay.workathome.domain.model.Milestone(trimmed)
    }
    val targetMinutes = targetTimeHours.toIntOrNull()?.let { it * 60 } ?: 0
    val reminder = ReminderSettings(
        frequency = reminderFrequency,
        timeOfDayMillis = parseReminderTime(reminderTime),
        customIntervalDays = recurrenceIntervalDays,
        notifyAllImmediately = urgency == TaskUrgency.ULTRA,
        recipients = recipients.toList()
    )
    val progress = when (progressType) {
        ProgressType.ONE_TIME -> TaskProgress()
        ProgressType.PERCENTAGE -> TaskProgress(progressValue = 0)
        ProgressType.MILESTONES -> TaskProgress(milestones = milestones)
        ProgressType.TIME_BASED -> TaskProgress(targetTimeMinutes = targetMinutes)
    }
    val id = taskId ?: UUID.randomUUID().toString()
    val global = globalId ?: name.lowercase().replace(" ", "-")
    val creatorName = createdBy.ifBlank { "Unknown" }
    return Task(
        id = id,
        globalId = global,
        houseId = houseId,
        taskName = name,
        createdAt = now,
        deadlineAt = deadlineMillis,
        reminderSettings = reminder,
        description = description.ifBlank { null },
        urgency = urgency,
        isActive = true,
        inProgress = false,
        isRecurring = isRecurring,
        recurringDeadlineAt = recurrenceIntervalDays?.let { interval -> deadlineMillis?.plus(interval * MILLIS_IN_DAY) },
        createdBy = creatorName,
        assignedTo = assignedTo,
        progress = progress,
        progressType = progressType,
        lastModifiedAt = now,
        lastSyncedAt = now,
        isDraft = markAsDraft
    )
}

private fun parseReminderTime(value: String): Long? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour * 60L * 60L * 1000L + minute * 60L * 1000L
}
