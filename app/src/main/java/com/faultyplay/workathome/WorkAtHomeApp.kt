package com.faultyplay.workathome

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.faultyplay.workathome.data.datastore.ThemePreference
import com.faultyplay.workathome.ui.AppViewModel
import com.faultyplay.workathome.ui.navigation.AppNavHost
import com.faultyplay.workathome.ui.theme.WorkAtHomeTheme

@Composable
fun WorkAtHomeApp(modifier: Modifier = Modifier) {
    val appViewModel: AppViewModel = hiltViewModel()
    val themePreference by appViewModel.themePreference.collectAsStateWithLifecycle()
    val darkTheme = when (themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    WorkAtHomeTheme(darkTheme = darkTheme) {
        Surface {
            AppNavHost(modifier = modifier)
        }
    }
}
