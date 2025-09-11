package com.faultyplay.workathome.house

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HouseSelectionScreen(
    viewModel: HouseViewModel = hiltViewModel(),
    onCreateHouse: () -> Unit,
    onJoinHouse: () -> Unit,
    onHouseSelected: () -> Unit
) {
    val state by viewModel.houseSelectionState.collectAsState()

    LaunchedEffect(key1 = state.hasHouses) {
        if (state.hasHouses) {
            onHouseSelected()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text(text = "Error: ${state.error}")
        } else if (!state.hasHouses) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome! You are not part of any house yet.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCreateHouse) {
                    Text("Create a House")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onJoinHouse) {
                    Text("Join a House")
                }
            }
        }
    }
}
