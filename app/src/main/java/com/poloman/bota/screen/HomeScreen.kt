package com.poloman.bota.screen

import androidx.compose.runtime.Composable
import com.poloman.bota.network.Communicator
import com.poloman.bota.views.HomePage


@Composable
fun HomeScreen(communicator: Communicator) {
    HomePage(communicator)
}