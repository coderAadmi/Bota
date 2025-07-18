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

    lateinit var onDisconnect : () -> Unit

    @RequiresApi(Build.VERSION_CODES.R)
    constructor(host : String, port : Int){
        this.host = host
        this.port = port
        connectToServer()
    }

    constructor(socket: Socket, onDisconnect : () -> Unit = {}){
        this.socket = socket
        this.host = ""
        bos = BufferedOutputStream(socket.outputStream)
        bos.flush()
        bis = BufferedInputStream(socket.inputStream)
    }

    fun setCallback(clientCallBack: BotaUser.BotaClientCallback){
        callback = clientCallBack
        transferStrategy.setCallback(callback)
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
            onDisconnect()
        }
        catch (e : IOException){
            Log.d("BTU_CLI_IOEX",e.toString())
            onDisconnect()
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun startListening(onNameAsked : (serverName : String) -> Unit = {}){
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
                        val serverName = result.result.substringAfter("UNAME ")
                        Log.d("BTU_SENDING_NAME", "To server $serverName")
                        sendCommand("UNAME Poloman-Android")
                        onNameAsked(serverName)
                    }
                    else if(result.result.startsWith("FILE_INCOMING_PERMISSION")){
                        val fileName = result.result.substringAfter("FILE_INCOMING_PERMISSION ")
                        val sizeResult = recv() as Result.CommandResponse
                        val size = sizeResult.result.substringAfter("FILE_SIZE ").toLong()
                        callback.onFileIncomingRequest(fileName,size)
                    }
                    else if(result.result.startsWith("MULTIPLE_FILE_INCOMING_PERMISSION")){
                        val fileCount = result.result.substringAfter("MULTIPLE_FILE_INCOMING_PERMISSION ").toInt()
                        val sizeResult = recv() as Result.CommandResponse
                        val size = sizeResult.result.substringAfter("FILE_SIZE ").toLong()
                        callback.onMultipleFileIncomingRequest(fileCount,size)
                    }
                }
            }
            catch (e : Exception){
                //socketException & IOException
                Log.d("BTU_CLIENT","Disconnected ${e.toString()}")
                onDisconnect()
            }
            //try catch
        }
    }

    @SuppressLint("NewApi")
    fun initFileReceiver(fileName :String, size : Long){
        sendCommand("OK $fileName")
    }

    @SuppressLint("NewApi")
    fun initFileReceiver(fcount :Int, size : Long){
        transferStrategy.setFileIncomingSize(size)
        sendCommand("OK $size")
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
            Log.d("BTU_BC_CLOSE",e.toString())
        }
    }

    fun askPermissionToSendFile(file: File){
        transferStrategy.askPermissionToSendFile(file,bos,bis)
    }

    fun askPermissionToSendFiles(files : List<File>){
        transferStrategy.askPermissionToSendFiles(files, bos, bis)
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

    fun denyFile(ip: String){
        sendCommand("DENIED")
    }
}