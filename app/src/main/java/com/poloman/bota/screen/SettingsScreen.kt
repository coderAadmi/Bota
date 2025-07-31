package com.poloman.bota.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poloman.bota.network.Communicator
import com.poloman.bota.views.SettingsPage

@Composable
fun SettingsScreen(communicator: Communicator) {
    Column(modifier = Modifier.fillMaxSize().padding(bottom = 1.dp)){
        SettingsPage(communicator)
    }
}