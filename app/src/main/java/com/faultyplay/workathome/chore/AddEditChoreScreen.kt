package com.faultyplay.workathome.chore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.faultyplay.workathome.data.Chore
import com.faultyplay.workathome.data.Urgency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEditChoreScreen(
    viewModel: ChoreViewModel = hiltViewModel(),
    houseId: String,
    onChoreSaved: () -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf(Urgency.MEDIUM) }
    var recurrenceInterval by remember { mutableStateOf("Weekly") }
    var deadlineStr by remember { mutableStateOf("") }
    val state by viewModel.choreState.collectAsState()

    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            onChoreSaved()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add/Edit Chore")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Task Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = deadlineStr, onValueChange = { deadlineStr = it }, label = { Text("Deadline (yyyy-MM-dd)") })
                Spacer(modifier = Modifier.height(8.dp))
                // Dropdown for Urgency and Recurrence would be better, using text for now
                OutlinedTextField(value = urgency.name, onValueChange = { urgency = Urgency.valueOf(it) }, label = { Text("Urgency (LOW, MEDIUM, HIGH, ULTRA)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = recurrenceInterval, onValueChange = { recurrenceInterval = it }, label = { Text("Recurrence (Weekly, Monthly, Quarterly)") })
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val deadline = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(deadlineStr)
                    val chore = Chore(
                        taskName = taskName,
                        description = description,
                        urgency = urgency,
                        deadline = deadline,
                        recurrenceInterval = recurrenceInterval
                    )
                    viewModel.saveChore(chore, houseId)
                }) {
                    Text("Save Chore")
                }
                state.error?.let {
                    Text("Error: $it")
                }
            }
        }
    }
}
