package com.poloman.bota.network

sealed class NetworkResponse {
    object Nothing : NetworkResponse()
    data class ConnectionRequest(val name : String, val ip : String) : NetworkResponse()
    data class IncomingDataRequest(val name : String, val ip : String, val filename : String,  val size : Long) : NetworkResponse()
    data class IncomingMulDataRequest(val name : String, val ip : String, val fileCount : Int,  val size : Long) : NetworkResponse()
}