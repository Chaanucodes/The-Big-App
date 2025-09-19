package com.faultyplay.workathome.ui.house

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HouseListRoute(
    onHouseSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HouseListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onSelect: (String) -> Unit = { houseId ->
        viewModel.selectHouse(houseId)
        onHouseSelected(houseId)
    }
    HouseListScreen(
        state = state,
        onInviteCodeChanged = viewModel::onInviteCodeChanged,
        onNewHouseNameChanged = viewModel::onNewHouseNameChanged,
        onAllowedContactsChanged = viewModel::onAllowedContactsChanged,
        onJoinHouse = viewModel::joinHouse,
        onCreateHouse = viewModel::createHouse,
        onSelectHouse = onSelect,
        modifier = modifier
    )
}

@Composable
fun HouseListScreen(
    state: HouseListUiState,
    onInviteCodeChanged: (String) -> Unit,
    onNewHouseNameChanged: (String) -> Unit,
    onAllowedContactsChanged: (String) -> Unit,
    onJoinHouse: () -> Unit,
    onCreateHouse: () -> Unit,
    onSelectHouse: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Hi, ${state.currentMember?.name ?: "there"}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Choose a house or create a new one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.houses.isNotEmpty()) {
            items(state.houses, key = { it.id }) { house ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.activeHouseId == house.id) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = house.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Members: ${house.members.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onSelectHouse(house.id) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading
                        ) {
                            Text(if (state.activeHouseId == house.id) "Open" else "Switch")
                        }
                    }
                }
            }
            item { Divider(modifier = Modifier.fillMaxWidth()) }
        }
        item {
            Text(
                text = "Join an existing house",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.inviteCode,
                onValueChange = onInviteCodeChanged,
                label = { Text("Invite code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onJoinHouse,
                enabled = state.inviteCode.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join house")
            }
        }
        item {
            Divider(modifier = Modifier.fillMaxWidth())
        }
        item {
            Text(
                text = "Create a new house",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.newHouseName,
                onValueChange = onNewHouseNameChanged,
                label = { Text("House name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.allowedContacts,
                onValueChange = onAllowedContactsChanged,
                label = { Text("Allowed emails or phone numbers (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = "Only listed contacts can join with your invite code",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onCreateHouse,
                enabled = state.newHouseName.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create house")
            }
        }
        if (state.errorMessage != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
