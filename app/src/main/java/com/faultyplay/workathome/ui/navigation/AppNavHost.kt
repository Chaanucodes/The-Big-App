package com.faultyplay.workathome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.faultyplay.workathome.ui.auth.AuthRoute
import com.faultyplay.workathome.ui.history.TaskHistoryRoute
import com.faultyplay.workathome.ui.home.HomeRoute
import com.faultyplay.workathome.ui.house.HouseListRoute
import com.faultyplay.workathome.ui.settings.SettingsRoute
import com.faultyplay.workathome.ui.task.TaskEditorRoute

object Destinations {
    const val AUTH = "auth"
    const val HOUSES = "houses"
    const val HOME = "home"
    const val TASK_EDITOR = "task_editor"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destinations.AUTH,
        modifier = modifier
    ) {
        composable(Destinations.AUTH) {
            AuthRoute(onAuthenticated = {
                navController.navigate(Destinations.HOUSES) {
                    popUpTo(Destinations.AUTH) { inclusive = true }
                }
            })
        }
        composable(Destinations.HOUSES) {
            HouseListRoute(
                onHouseSelected = { houseId ->
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.HOUSES) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.HOME) {
            HomeRoute(
                onAddTask = { houseId ->
                    navController.navigate("${Destinations.TASK_EDITOR}?houseId=${houseId ?: ""}")
                },
                onOpenSettings = {
                    navController.navigate(Destinations.SETTINGS)
                },
                onOpenHistory = {
                    navController.navigate(Destinations.HISTORY)
                }
            )
        }
        composable(
            route = "${Destinations.TASK_EDITOR}?houseId={houseId}&taskId={taskId}",
            arguments = listOf(
                navArgument("houseId") { type = NavType.StringType; defaultValue = "" },
                navArgument("taskId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            TaskEditorRoute(
                onNavigateBack = { navController.popBackStack() },
                onTaskSaved = {}
            )
        }
        composable(Destinations.SETTINGS) {
            SettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onManageHouses = {
                    navController.navigate(Destinations.HOUSES) {
                        popUpTo(Destinations.HOME) { inclusive = false }
                    }
                },
                onSignedOut = {
                    navController.navigate(Destinations.AUTH) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Destinations.HISTORY) {
            TaskHistoryRoute(onNavigateBack = { navController.popBackStack() })
        }
    }
}
