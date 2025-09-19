package com.faultyplay.workathome.ui.settings

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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.faultyplay.workathome.data.datastore.ThemePreference
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.faultyplay.workathome.R

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onManageHouses: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onNotificationsToggled = viewModel::setNotificationsEnabled,
        onThemeChanged = viewModel::setThemePreference,
        onSelectHouse = viewModel::selectHouse,
        onManageHouses = onManageHouses,
        onSignOut = {
            viewModel.signOut(onSignedOut)
        },
        modifier = modifier
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onNavigateBack: () -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onThemeChanged: (ThemePreference) -> Unit,
    onSelectHouse: (String) -> Unit,
    onManageHouses: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Notifications", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable task reminders")
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = onNotificationsToggled
                        )
                    }
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
                    ThemePreference.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(theme.name.lowercase().replaceFirstChar { it.uppercase() })
                            Switch(
                                checked = state.themePreference == theme,
                                onCheckedChange = { checked -> if (checked) onThemeChanged(theme) }
                            )
                        }
                    }
                }
            }
            item { Divider() }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Houses", style = MaterialTheme.typography.titleMedium)
                    if (state.houses.isEmpty()) {
                        Text(
                            text = "No houses available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.houses.forEach { house ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(house.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "Members: ${house.members.size}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                TextButton(onClick = { onSelectHouse(house.id) }) {
                                    Text("Switch")
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onManageHouses) { Text("Manage houses") }
                }
            }
            item { Divider() }
            item {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSigningOut
                ) {
                    Text(if (state.isSigningOut) "Signing out..." else "Sign out")
                }
            }
        }
    }
}
