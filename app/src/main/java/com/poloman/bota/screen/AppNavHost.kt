package com.poloman.bota.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.poloman.bota.network.Communicator

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    communicator: Communicator
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> HomeScreen(communicator)
                    Destination.TRANSFER -> TransferScreen(communicator)
                    Destination.SETTINGS -> SettingsScreen(communicator)
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