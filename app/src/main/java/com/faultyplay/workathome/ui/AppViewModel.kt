package com.faultyplay.workathome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faultyplay.workathome.data.datastore.ThemePreference
import com.faultyplay.workathome.data.datastore.UserPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppViewModel @Inject constructor(
    preferences: UserPreferencesDataSource
) : ViewModel() {
    val themePreference: StateFlow<ThemePreference> = preferences.themePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.SYSTEM)
}
