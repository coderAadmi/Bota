package com.poloman.bota.network

sealed class Response{
    data class CommandReceived(val cmd : String) : Response()
    data class Error(val e : Exception) : Response()
    object Blank : Response()
}