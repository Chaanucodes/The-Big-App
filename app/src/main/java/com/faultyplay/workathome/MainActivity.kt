package com.faultyplay.workathome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.faultyplay.workathome.navigation.AppNavHost
import com.faultyplay.workathome.ui.theme.HouseChoreAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HouseChoreAppTheme {
                AppNavHost()
            }
        }
    }
}