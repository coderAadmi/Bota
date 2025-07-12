package com.poloman.bota.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> HomeScreen()
                    Destination.TRANSFER -> TransferScreen(navController)
                    Destination.SETTINGS -> SettingsScreen()
                }
            }
        }
        composable("files?text={contentType}",
            arguments = listOf(
                navArgument("contentType") {
                    type = NavType.IntType
                    nullable = false
                }
            )) {
            FileScreen(navController, it.arguments!!.getInt("contentType"))
        }
    }
}