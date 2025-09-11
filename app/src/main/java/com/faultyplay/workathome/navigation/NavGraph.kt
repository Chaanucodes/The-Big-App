package com.faultyplay.workathome.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.faultyplay.workathome.auth.SignInScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.faultyplay.workathome.chore.AddEditChoreScreen
import com.faultyplay.workathome.dashboard.DashboardScreen
import com.faultyplay.workathome.house.HouseSelectionScreen
import com.faultyplay.workathome.house.CreateHouseScreen
import com.faultyplay.workathome.house.JoinHouseScreen

object Routes {
    const val SIGN_IN = "sign_in"
    const val HOUSE_SELECTION = "house_selection"
    const val CREATE_HOUSE = "create_house"
    const val JOIN_HOUSE = "join_house"
import androidx.navigation.NavType
import androidx.navigation.navArgument

object Routes {
    const val SIGN_IN = "sign_in"
    const val HOUSE_SELECTION = "house_selection"
    const val CREATE_HOUSE = "create_house"
    const val JOIN_HOUSE = "join_house"
    const val DASHBOARD = "dashboard"
    const val ADD_EDIT_CHORE = "add_edit_chore/{houseId}"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SIGN_IN) {
        composable(Routes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Routes.HOUSE_SELECTION) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOUSE_SELECTION) {
            HouseSelectionScreen(
                onCreateHouse = { navController.navigate(Routes.CREATE_HOUSE) },
                onJoinHouse = { navController.navigate(Routes.JOIN_HOUSE) },
                onHouseSelected = { navController.navigate(Routes.DASHBOARD) }
            )
        }
        composable(Routes.CREATE_HOUSE) {
            CreateHouseScreen(
                onHouseCreated = {
                    navController.navigate(Routes.HOUSE_SELECTION) {
                        popUpTo(Routes.HOUSE_SELECTION) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.JOIN_HOUSE) {
            JoinHouseScreen(
                onHouseJoined = {
                    navController.navigate(Routes.HOUSE_SELECTION) {
                        popUpTo(Routes.HOUSE_SELECTION) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddChore = { houseId ->
                    navController.navigate(Routes.ADD_EDIT_CHORE.replace("{houseId}", houseId))
                }
            )
        }
        composable(
            route = Routes.ADD_EDIT_CHORE,
            arguments = listOf(navArgument("houseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val houseId = backStackEntry.arguments?.getString("houseId") ?: ""
            AddEditChoreScreen(
                houseId = houseId,
                onChoreSaved = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}
