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
fun JoinHouseScreen(
    viewModel: HouseViewModel = hiltViewModel(),
    onHouseJoined: () -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }
    val state by viewModel.joinHouseState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            android.widget.Toast.makeText(context, "Successfully joined house!", android.widget.Toast.LENGTH_SHORT).show()
            onHouseJoined()
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
                Text("Join an Existing House")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("Invite Code") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.joinHouse(inviteCode) }) {
                    Text("Join House")
                }
                state.error?.let {
                    Text("Error: $it")
                }
            }
        }
    }
}
