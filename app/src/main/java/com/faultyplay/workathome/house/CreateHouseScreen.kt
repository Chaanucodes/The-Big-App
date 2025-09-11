package com.faultyplay.workathome.house

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateHouseScreen(
    viewModel: HouseViewModel = hiltViewModel(),
    onHouseCreated: () -> Unit
) {
    var houseName by remember { mutableStateOf("") }
    var invitedEmails by remember { mutableStateOf("") }
    val state by viewModel.createHouseState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            android.widget.Toast.makeText(context, "House created!", android.widget.Toast.LENGTH_SHORT).show()
            onHouseCreated()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Create a New House")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = houseName,
                    onValueChange = { houseName = it },
                    label = { Text("House Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = invitedEmails,
                    onValueChange = { invitedEmails = it },
                    label = { Text("Invited Member Emails (comma-separated)") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val emails = invitedEmails.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    viewModel.createHouse(houseName, emails)
                }) {
                    Text("Create House")
                }
                state.error?.let {
                    Text("Error: $it")
                }
            }
        }
    }
}
