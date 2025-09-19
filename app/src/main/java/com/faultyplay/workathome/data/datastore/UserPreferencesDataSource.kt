package com.faultyplay.workathome.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val SelectedHouse = stringPreferencesKey("selected_house")
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val Theme = stringPreferencesKey("theme")
        val LastUserId = stringPreferencesKey("last_user_id")
    }

    val selectedHouseId: Flow<String?> = dataStore.data.map { it[Keys.SelectedHouse] }
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.NotificationsEnabled] ?: true
    }
    val themePreference: Flow<ThemePreference> = dataStore.data.map { preferences ->
        preferences[Keys.Theme]?.let { ThemePreference.valueOf(it) } ?: ThemePreference.SYSTEM
    }
    val lastUserId: Flow<String?> = dataStore.data.map { it[Keys.LastUserId] }

    suspend fun setSelectedHouse(id: String?) {
        dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(Keys.SelectedHouse)
            } else {
                prefs[Keys.SelectedHouse] = id
            }
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NotificationsEnabled] = enabled }
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        dataStore.edit { it[Keys.Theme] = themePreference.name }
    }

    suspend fun setLastUserId(userId: String?) {
        dataStore.edit { prefs ->
            if (userId == null) {
                prefs.remove(Keys.LastUserId)
            } else {
                prefs[Keys.LastUserId] = userId
            }
        }
    }
}
