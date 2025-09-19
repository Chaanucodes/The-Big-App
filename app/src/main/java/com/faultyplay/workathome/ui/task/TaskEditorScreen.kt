package com.faultyplay.workathome.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.faultyplay.workathome.R
import com.faultyplay.workathome.domain.model.ProgressType
import com.faultyplay.workathome.domain.model.ReminderFrequency
import com.faultyplay.workathome.domain.model.TaskSuggestion
import com.faultyplay.workathome.domain.model.TaskUrgency

@Composable
fun TaskEditorRoute(
    onNavigateBack: () -> Unit,
    onTaskSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TaskEditorScreen(
        state = state,
        onNameChanged = viewModel::onNameChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onUrgencyChanged = viewModel::onUrgencyChanged,
        onProgressTypeChanged = viewModel::onProgressTypeChanged,
        onDeadlineDaysChanged = viewModel::onDeadlineDaysChanged,
        onRecurringChanged = viewModel::onRecurringToggle,
        onRecurrenceIntervalChanged = viewModel::onRecurrenceIntervalChanged,
        onReminderFrequencyChanged = viewModel::onReminderFrequencyChanged,
        onReminderTimeChanged = viewModel::onReminderTimeChanged,
        onRecipientToggled = viewModel::onRecipientToggled,
        onAssignedMemberChanged = viewModel::onAssignedMemberChanged,
        onMilestonesChanged = viewModel::onMilestonesChanged,
        onTargetHoursChanged = viewModel::onTargetHoursChanged,
        onApplySuggestion = viewModel::applySuggestion,
        onSaveDraft = viewModel::saveDraft,
        onDiscard = {
            viewModel.discardDraft()
            onNavigateBack()
        },
        onSave = {
            viewModel.saveTask {
                onTaskSaved()
                onNavigateBack()
            }
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    state: TaskEditorUiState,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onUrgencyChanged: (TaskUrgency) -> Unit,
    onProgressTypeChanged: (ProgressType) -> Unit,
    onDeadlineDaysChanged: (String) -> Unit,
    onRecurringChanged: (Boolean) -> Unit,
    onRecurrenceIntervalChanged: (String) -> Unit,
    onReminderFrequencyChanged: (ReminderFrequency) -> Unit,
    onReminderTimeChanged: (String) -> Unit,
    onRecipientToggled: (String) -> Unit,
    onAssignedMemberChanged: (String?) -> Unit,
    onMilestonesChanged: (String) -> Unit,
    onTargetHoursChanged: (String) -> Unit,
    onApplySuggestion: (TaskSuggestion) -> Unit,
    onSaveDraft: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.draft.taskId == null) "Add chore" else "Edit chore",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SuggestionSection(state = state, onApplySuggestion = onApplySuggestion)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.draft.name,
                onValueChange = onNameChanged,
                label = { Text("Task name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.draft.description,
                onValueChange = onDescriptionChanged,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(12.dp))
            Text(text = "Urgency", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskUrgency.entries.forEach { urgency ->
                    FilterChip(
                        selected = state.draft.urgency == urgency,
                        onClick = { onUrgencyChanged(urgency) },
                        label = { Text(urgency.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Progress type", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgressType.entries.forEach { progress ->
                    FilterChip(
                        selected = state.draft.progressType == progress,
                        onClick = { onProgressTypeChanged(progress) },
                        label = { Text(progress.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            when (state.draft.progressType) {
                ProgressType.MILESTONES -> {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draft.milestoneText,
                        onValueChange = onMilestonesChanged,
                        label = { Text("Milestones (one per line)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                ProgressType.TIME_BASED -> {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draft.targetTimeHours,
                        onValueChange = onTargetHoursChanged,
                        label = { Text("Target hours") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> Unit
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Deadline", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.draft.deadlineDays,
                onValueChange = onDeadlineDaysChanged,
                label = { Text("Days from today") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = state.draft.isRecurring, onClick = { onRecurringChanged(!state.draft.isRecurring) })
                Text("Recurring")
            }
            if (state.draft.isRecurring) {
                OutlinedTextField(
                    value = state.draft.recurrenceIntervalDays?.toString().orEmpty(),
                    onValueChange = onRecurrenceIntervalChanged,
                    label = { Text("Repeat every (days)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Reminder", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderFrequency.entries.forEach { frequency ->
                    AssistChip(
                        onClick = { onReminderFrequencyChanged(frequency) },
                        label = { Text(frequency.name.lowercase().replace('_', ' ')) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.draft.reminderFrequency == frequency) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.draft.reminderTime,
                onValueChange = onReminderTimeChanged,
                label = { Text("Reminder time (HH:MM)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text(text = "Notify members", style = MaterialTheme.typography.titleMedium)
            if (state.members.isEmpty()) {
                Text(
                    text = "No members available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.members.forEach { member ->
                        val selected = member.id in state.draft.recipients
                        ElevatedAssistChip(
                            onClick = { onRecipientToggled(member.id) },
                            label = { Text(member.name) },
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check),
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Assign to", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedAssistChip(
                    onClick = { onAssignedMemberChanged(null) },
                    label = { Text("Unassigned") },
                    leadingIcon = {
                        if (state.draft.assignedTo == null) {
                            Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null)
                        }
                    }
                )
                state.members.forEach { member ->
                    ElevatedAssistChip(
                        onClick = { onAssignedMemberChanged(member.id) },
                        label = { Text(member.name) },
                        leadingIcon = {
                            if (state.draft.assignedTo == member.id) {
                                Icon(painter = painterResource(id = R.drawable.ic_check), contentDescription = null)
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDiscard) { Text("Discard") }
                TextButton(onClick = onSaveDraft) { Text("Save draft") }
                Spacer(Modifier.weight(1f))
                Button(onClick = onSave, enabled = !state.isSaving) { Text("Save task") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SuggestionSection(
    state: TaskEditorUiState,
    onApplySuggestion: (TaskSuggestion) -> Unit
) {
    if (state.suggestions.isEmpty()) return
    Column {
        Text(text = "Suggestions", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.suggestions.take(6).forEach { suggestion ->
                AssistChip(onClick = { onApplySuggestion(suggestion) }, label = { Text(suggestion.name) })
            }
        }
    }
}
