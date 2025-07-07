package com.poloman.bota.network

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException

class BotaClient {

    private lateinit var socket: Socket
    private var host: String
    private var port = 0

    private lateinit var bos : BufferedOutputStream
    private lateinit var bis : BufferedInputStream

    val transferStrategy = BotaTransferStrategy()

    private val _inputState  = MutableStateFlow<Response>(Response.Blank)
    val inputState  = _inputState.asStateFlow()

    private var isListening = true

    constructor( host : String,  port : Int){
        this.host = host
        this.port = port
    }

    constructor(socket: Socket){
        this.socket = socket
        this.host = ""

        bos = BufferedOutputStream(socket.outputStream)
        bos.flush()
        bis = BufferedInputStream(socket.inputStream)
        Log.d("BTU_CL","Welcome here")
    }

    fun connectToServer(){
        try {
            socket = Socket(host,port)
            bos = BufferedOutputStream(socket.outputStream)
            bos.flush()
            bis = BufferedInputStream(socket.inputStream)
        }
        catch (e : UnknownHostException){
            Log.d("CLI_UEX",e.toString())
        }
        catch (e : IOException){
            Log.d("CLI_IOEX",e.toString())
        }
    }


    fun recv() : String{
        val res =  transferStrategy.recvCommand(bos,bis)
        return res.toString()
    }

    fun stopReceiving(){
        isListening = false
    }

    fun closeConnection(){
        try {
            bos.close()
            bis.close()
            socket.close()
        }
        catch (e : IOException){
        }
    }

    fun sendFile(file : File){
        transferStrategy.sendFile(file, bos,bis)
    }

    fun sendDir(dir: String) {
        transferStrategy.sendCommand("DIR $dir",bos,bis)
    }
}