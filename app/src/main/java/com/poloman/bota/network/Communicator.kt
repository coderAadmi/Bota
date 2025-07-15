package com.poloman.bota.network

import androidx.compose.runtime.Composable

interface Communicator {
    fun onStartServer()
    fun onConnectToServer(ip : String)
    fun showConnectedDevices()
}