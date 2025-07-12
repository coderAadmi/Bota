package com.poloman.bota.network

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.poloman.bota.BotaUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private lateinit var callback: BotaUser.BotaClientCallback

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
    }

    fun setCallback(permissionCallback: BotaUser.BotaClientCallback){
        callback = permissionCallback
    }


    fun connectToServer(){
        try {
            socket = Socket(host,port)
            bos = BufferedOutputStream(socket.outputStream)
            bos.flush()
            bis = BufferedInputStream(socket.inputStream)
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
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
                    else if(result.result.startsWith("UNAME")){
                        sendCommand("UNAME Poloman-Android")
                    }
                    else if(result.result.startsWith("FILE_INCOMING_PERMISSION")){
                        val fileName = result.result.substringAfter("FILE_INCOMING_PERMISSION ")
                        val sizeResult = recv() as Result.CommandResponse
                        val size = sizeResult.result.substringAfter("FILE_SIZE ").toLong()
                        callback.onFileIncomingRequest(fileName,size)
                    }
                }
            }
            catch (e : Exception){
                //socketException & IOException
                Log.d("BTU_CLIENT","Disconnected ${e.toString()}")
            }
            //try catch
        }
    }

    @SuppressLint("NewApi")
    fun initFileReceiver(fileName :String, size : Long){
        sendCommand("OK $fileName")
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

    suspend fun sendFile(file : File){
        transferStrategy.sendFile(file, bos,bis)
    }

    fun sendDir(dir: String) {
        transferStrategy.sendCommand("DIR $dir",bos,bis)
    }

    fun sendCommand(command: String) {
        transferStrategy.sendCommand(command,bos,bis)
    }

    fun getIpAddress() : String{
        return socket.inetAddress.toString()
    }

}