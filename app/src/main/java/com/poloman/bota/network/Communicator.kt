package com.poloman.bota.network


interface Communicator {
    fun onStartServer()
    fun onConnectToServer(ip : String)
    fun showUserSelector()
    fun showConnectedDevices()
}