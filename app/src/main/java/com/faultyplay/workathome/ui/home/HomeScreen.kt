package com.faultyplay.workathome.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.faultyplay.workathome.R
import com.faultyplay.workathome.domain.model.ProgressType
import com.faultyplay.workathome.domain.model.Task
import com.faultyplay.workathome.domain.model.TaskUrgency

@Composable
fun HomeRoute(
    onAddTask: (String?) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onSearchQueryChange = viewModel::setSearchQuery,
        onAddTask = { onAddTask(state.house?.id) },
        onOpenSettings = onOpenSettings,
        onOpenHistory = onOpenHistory,
        onStartTask = viewModel::startTask,
        onStopTask = viewModel::stopTask,
        onCompleteTask = viewModel::completeTask,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onStartTask: (Task) -> Unit,
    onStopTask: (Task) -> Unit,
    onCompleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hi, ${state.currentMemberName ?: "there"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        state.house?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add task"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            TextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("Search tasks") },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onOpenHistory) {
                    Text("History")
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.activeTasks.isEmpty()) {
                    item {
                        Text(
                            text = "No active tasks. Tap + to add a new one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(state.activeTasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onStartTask = onStartTask,
                            onStopTask = onStopTask,
                            onCompleteTask = onCompleteTask
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onStartTask: (Task) -> Unit,
    onStopTask: (Task) -> Unit,
    onCompleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (task.urgency) {
                TaskUrgency.ULTRA -> MaterialTheme.colorScheme.errorContainer
                TaskUrgency.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                TaskUrgency.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                TaskUrgency.LOW -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.taskName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    task.deadlineAt?.let { deadline ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Due: ${formatDate(deadline)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    task.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            TaskProgressContent(task = task)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (task.progressType) {
                    ProgressType.ONE_TIME -> {
                        TextButton(onClick = { onCompleteTask(task) }) {
                            Text("Mark complete")
                        }
                    }
                    else -> {
                        if (task.inProgress) {
                            TextButton(onClick = { onStopTask(task) }) {
                                Text("Pause")
                            }
                        } else {
                            TextButton(onClick = { onStartTask(task) }) {
                                Text("Start")
                            }
                        }
                        TextButton(onClick = { onCompleteTask(task) }) {
                            Text("Complete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskProgressContent(task: Task, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (task.progressType) {
            ProgressType.ONE_TIME -> {
                val color = if (task.inProgress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Text(
                    text = if (task.inProgress) "In progress" else "Ready to start",
                    color = color,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            ProgressType.PERCENTAGE -> {
                LinearProgressIndicator(
                    progress = (task.progress.progressValue / 100f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${task.progress.progressValue}% complete",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            ProgressType.MILESTONES -> {
                val total = task.progress.milestones.size.coerceAtLeast(1)
                val completed = task.progress.milestones.count { it.isCompleted }
                Text(
                    text = "$completed / $total milestones complete",
                    style = MaterialTheme.typography.bodySmall
                )
                task.progress.milestones.getOrNull(completed)?.let { milestone ->
                    Text(
                        text = "Next: ${milestone.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            ProgressType.TIME_BASED -> {
                val ratio = if (task.progress.targetTimeMinutes <= 0) 0f else
                    (task.progress.trackedTimeMinutes.toFloat() / task.progress.targetTimeMinutes).coerceIn(0f, 1f)
                LinearProgressIndicator(progress = ratio, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${task.progress.trackedTimeMinutes} / ${task.progress.targetTimeMinutes} mins",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.time.Instant.ofEpochMilli(timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    return date.toString()
}
