package com.poloman.bota.network

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    @RequiresApi(Build.VERSION_CODES.R)
    constructor(host : String, port : Int){
        this.host = host
        this.port = port
        connectToServer()
    }

    constructor(socket: Socket){
        this.socket = socket
        this.host = ""

        bos = BufferedOutputStream(socket.outputStream)
        bos.flush()
        bis = BufferedInputStream(socket.inputStream)
        Log.d("BTU_CL","Welcome here")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun connectToServer(){
        try {
            socket = Socket(host,port)
            bos = BufferedOutputStream(socket.outputStream)
            bos.flush()
            bis = BufferedInputStream(socket.inputStream)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    startListening()
                }
                catch (e : Exception){
                    //socketException & IOException
                }
                 //try catch
            }
        }
        catch (e : UnknownHostException){
            Log.d("BTU_CLI_UEX",e.toString())
        }
        catch (e : IOException){
            Log.d("BTU_CLI_IOEX",e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startListening(){
        while (isListening) {
            val result = recv() as Result.CommandResponse
            if(result.result.startsWith("RCV_FILE")){
                val fileName = result.result.substringAfter("RCV_FILE ")
                sendCommand("SND_SIZE")
                val sizeResult = recv() as Result.CommandResponse
                val size = sizeResult.result.toLong()
                sendCommand("SND_FILE $fileName")
                transferStrategy.recvFile(fileName,size,bos,bis)
            }
            else if(result.result.startsWith("DIR")){
                val fileName = result.result.substringAfter("DIR ")
                val root = ""
                val file = File("$root$fileName")
                file.mkdirs();
                Log.d("BTU_DIR","Directory created $fileName")
                sendCommand("DIR_CREATED $fileName")
            }
        }
    }


    fun recv() : Result{
        val res =  transferStrategy.recvCommand(bos,bis)
        return res
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

    fun sendCommand(command: String) {
        transferStrategy.sendCommand(command,bos,bis)
    }

}