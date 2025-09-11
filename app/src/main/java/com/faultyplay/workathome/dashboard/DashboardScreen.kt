package com.faultyplay.workathome.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.faultyplay.workathome.data.Chore

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAddChore: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { state.houseId?.let { onAddChore(it) } }) {
                Icon(Icons.Default.Add, contentDescription = "Add Chore")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(text = "Error: ${state.error}")
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Button(onClick = { viewModel.changeSortType(SortType.URGENCY) }) {
                            Text("Sort by Urgency")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.changeSortType(SortType.DEADLINE) }) {
                            Text("Sort by Deadline")
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.chores) { chore ->
                            ChoreItem(
                                chore = chore,
                                onComplete = {
                                    state.houseId?.let { houseId ->
                                        viewModel.completeChore(chore, houseId)
                                    }
                                }
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun ChoreItem(
    chore: Chore,
    onComplete: () -> Unit
) {
    // Basic representation of a chore item
    Row(
        modifier = Modifier.padding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = chore.taskName)
            Text(text = "Urgency: ${chore.urgency}")
            Text(text = "Deadline: ${chore.deadline}")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onComplete) {
            Text("Complete")
        }
    }
}
