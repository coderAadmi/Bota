package com.poloman.bota.network

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File

sealed class Result{
    object Success : Result()
    data class Error(val e : Exception) : Result()
    data class CommandResponse(val result : String) : Result()
}

interface SendStrategy {
    fun sendFile(file : File, bos : BufferedOutputStream, bis : BufferedInputStream) :  Result
    fun sendCommand(cmd : String, bos : BufferedOutputStream, bis : BufferedInputStream) : Result
    fun sendDir(path : String, bos : BufferedOutputStream, bis : BufferedInputStream) : Result
    fun recvFile(fileName : String, size : Long, bos : BufferedOutputStream, bis : BufferedInputStream) : Result
    fun recvCommand(bos : BufferedOutputStream, bis : BufferedInputStream) : Result
}